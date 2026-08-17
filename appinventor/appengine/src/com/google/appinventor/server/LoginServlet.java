// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2019 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.server;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import com.google.appinventor.server.flags.Flag;

import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.server.storage.StoredData.PWData;
import com.google.appinventor.server.storage.StoredData.ProjectNotFoundException;

import com.google.appinventor.server.tokens.Token;
import com.google.appinventor.server.tokens.TokenException;
import com.google.appinventor.server.tokens.TokenProto;

import com.google.appinventor.server.util.PasswordHash;
import com.google.appinventor.server.util.UriBuilder;

import com.google.appinventor.shared.rpc.user.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

/**
 * LoginServlet -- Handle logging someone in using an email address for a login
 * name and a password, which is stored hashed (and salted). Facilities are
 * provided to e-mail a password to an e-mail address both to set one up the
 * first time and to recover a lost password.
 *
 * This implementation uses a helper server to send mail. It does a webservices
 * transaction (REST/POST) to the server with the email address and reset url.
 * The helper server then formats the e-mail message and sends it. The source
 * code is in misc/passwordmail/...
 *
 * @author jis@mit.edu (Jeffrey I. Schiller)
 */
@SuppressWarnings("unchecked")
public class LoginServlet extends HttpServlet {

  private final StorageIo storageIo = StorageIoInstanceHolder.getInstance();
  private static final Logger LOG = Logger.getLogger(LoginServlet.class.getName());
  private static final Flag<String> mailServer = Flag.createFlag("localauth.mailserver", "");
  private static final Flag<String> password = Flag.createFlag("localauth.mailserver.password", "");
  private static final Flag<Boolean> useGoogle = Flag.createFlag("auth.usegoogle", true);
  private static final Flag<Boolean> useLocal = Flag.createFlag("auth.uselocal", false);
  private static final String loginUrl = Flag.createFlag("login.url", "").get();
  private static final Flag<String> firebaseApiKey = Flag.createFlag("firebase.api.key", "AIzaSyCNMBDXRM7cCJHpoTkz8xPJ_yCRmn2LP4Q");
  private static final Flag<String> firebaseAuthDomain = Flag.createFlag("firebase.auth.domain", "");
  private static final Flag<String> firebaseProjectId = Flag.createFlag("firebase.project.id", "");
  private static final Flag<String> firebaseAppId = Flag.createFlag("firebase.app.id", "");

  private static final UserService userService = UserServiceFactory.getUserService();
  private final PolicyFactory sanitizer = new HtmlPolicyBuilder().allowElements("p").toFactory();
  private static final boolean DEBUG = Flag.createFlag("appinventor.debugging", false).get();

  private static final Set<LoginListener> loginListeners = new HashSet<>();

  public interface LoginListener {
    void onLogin(User user, TokenProto.token token);
  }

  public static void addLoginListener(LoginListener listener) {
    loginListeners.add(listener);
  }

  public static void removeLoginListener(LoginListener listener) {
    loginListeners.remove(listener);
  }

  public void init(ServletConfig config) throws ServletException {
    super.init(config);
  }

  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.setContentType("text/html; charset=utf-8");

    PrintWriter out;
    String [] components = req.getRequestURI().split("/");
    if (DEBUG) {
      LOG.info("requestURI = " + req.getRequestURI());
    }
    String page = getPage(req);

    OdeAuthFilter.UserInfo userInfo = OdeAuthFilter.getUserInfo(req);

    String queryString = req.getQueryString();
    HashMap<String, String> params = getQueryMap(queryString);
    // These params are passed around so they can take effect even if we
    // were not logged in.
    String locale = params.get("locale");
    String repo = params.get("repo");
    String galleryId = params.get("galleryId");
    String redirect = params.get("redirect");
    String autoload = params.get("autoload");
    String newGalleryId = params.get("ng");
    String uiPreference = params.get("ui");

    if (DEBUG) {
      LOG.info("locale = " + locale + " bundle: " + new Locale(locale));
    }
    ResourceBundle bundle;
    if (locale == null) {
      bundle = ResourceBundle.getBundle("com/google/appinventor/server/loginmessages", new Locale("en"));
    } else {
      bundle = ResourceBundle.getBundle("com/google/appinventor/server/loginmessages", new Locale(locale));
    }

    if (page.equals("google")) {
      // We get here after we have gone through the Google Login page
      // This is arranged via a security-constraint setup in web.xml
      com.google.appengine.api.users.User apiUser = userService.getCurrentUser();
      if (apiUser == null) {  // Hmmm. I don't think this should happen
        fail(req, resp, "Google Authentication Failed", locale); // Not sure what else to do
        return;
      }
      String email = apiUser.getEmail();
      String userId = apiUser.getUserId();
      User user = storageIo.getUser(userId, email);

      userInfo = new OdeAuthFilter.UserInfo(); // Create a new userInfo object

      userInfo.setUserId(user.getUserId()); // This effectively logs us in!
      userInfo.setIsAdmin(user.getIsAdmin());
      if (userService.isUserAdmin()) { // If we are a developer, we are always an admin
        userInfo.setIsAdmin(true);
      }

      String newCookie = userInfo.buildCookie(false);
      if (DEBUG) {
        LOG.info("newCookie = " + newCookie);
      }
      if (newCookie != null) {
        Cookie cook = new Cookie("AppInventor", newCookie);
        cook.setPath("/");
        resp.addCookie(cook);
      }
      // Remove the ACSID Cookie used by Google for Authentication
      Cookie cook = new Cookie("ACSID", null);
      cook.setPath("/");
      cook.setMaxAge(0);
      resp.addCookie(cook);
      String uri = "/";
      if (redirect != null) {
        uri = redirect;
      }
      uri = new UriBuilder(uri)
        .add("locale", locale)
        .add("repo", repo)
        .add("autoload", autoload)
        .add("ng", newGalleryId)
        .add("ui", uiPreference)
        .add("galleryId", galleryId).build();
      resp.sendRedirect(uri);
      return;
    } else {
      if (!loginUrl.isEmpty() && !page.equals("token")) {
        /* If we have an external login URL specified, then redirect to it. */
        String uri = new UriBuilder(loginUrl)
          .add("locale", locale)
          .add("repo", repo)
          .add("ng", newGalleryId)
          .add("galleryId", galleryId)
          .add("autoload", autoload)
          .add("ui", uiPreference)
          .add("redirect", redirect).build();
        resp.sendRedirect(uri);
        return;
      }
      if (useLocal.get() == false) {
        if (useGoogle.get() == false) {
          out = setCookieOutput(userInfo, resp);
          out.println("<html><head><title>Error</title></head>\n");
          out.println("<body><h1>App Inventor is Mis-Configured</h1>\n");
          out.println("<p>This instance of App Inventor has no authentication mechanism configured.</p>\n");
          out.println("</body>\n");
          out.println("</html>\n");
          return;
        } else if (!page.equals("token")) {
            String uri = new UriBuilder("/login/google")
              .add("locale", locale)
              .add("repo", repo)
              .add("ng", newGalleryId)
              .add("galleryId", galleryId)
              .add("autoload", autoload)
              .add("ui", uiPreference)
              .add("redirect", redirect).build();
            resp.sendRedirect(uri);
            return;
        }
      }
    }

    // If we get here, local accounts are supported
    // or we are the "token" page

    if (page.equals("setpw")) {
      String uid = getParam(req);
      if (uid == null) {
        fail(req, resp, "Invalid Set Password Link", locale);
        return;
      }
      PWData data = storageIo.findPWData(uid);
      if (data == null) {
        fail(req, resp, "Invalid Set Password Link", locale);
        return;
      }
      if (DEBUG) {
        LOG.info("setpw email = " + data.email);
      }
      req.setAttribute("view", "setpw");
      req.setAttribute("resetUid", uid);
      req.setAttribute("resetEmail", data.email);
      req.setAttribute("locale", locale);
      req.setAttribute("repo", repo);
      req.setAttribute("autoload", autoload);
      req.setAttribute("ng", newGalleryId);
      req.setAttribute("ui", uiPreference);
      req.setAttribute("galleryId", galleryId);
      req.setAttribute("redirect", redirect);
      try {
        req.getRequestDispatcher("/login.jsp").forward(req, resp);
      } catch (ServletException e) {
        throw new IOException(e);
      }
      return;
    } else if (page.equals("linksent")) {
      String emailParam = params.get("email");
      String uri = new UriBuilder("/login")
        .add("status", "linksent")
        .add("email", emailParam)
        .add("locale", locale)
        .add("repo", repo)
        .add("autoload", autoload)
        .add("ng", newGalleryId)
        .add("ui", uiPreference)
        .add("galleryId", galleryId)
        .add("redirect", redirect).build();
      resp.sendRedirect(uri);
      return;
    } else if (page.equals("sendlink")) {
      String uri = new UriBuilder("/login")
        .add("view", "forgot")
        .add("locale", locale)
        .add("repo", repo)
        .add("autoload", autoload)
        .add("ng", newGalleryId)
        .add("ui", uiPreference)
        .add("galleryId", galleryId)
        .add("redirect", redirect).build();
      resp.sendRedirect(uri);
      return;
    } else if (page.equals("token") || page.equals("stoken")) {
      String encodedToken = params.get("token");
      if (encodedToken == null) {
        fail(req, resp, "No Authentication Token Provided", locale);
        return;
      }
      TokenProto.token token = null;
      try {
        if (page.equals("token")) {
          token = Token.verifyToken(encodedToken);
        } else {
          token = Token.verifySToken(encodedToken);
        }
      } catch (TokenException e) {
        fail(req, resp, e.getMessage(), locale);
        return;
      }
      // At this point we have a valid token, so use it to login!
      // need to make sure it is a SSOLOGIN token
      if (token.getCommand() != TokenProto.token.CommandType.SSOLOGIN &&
          token.getCommand() != TokenProto.token.CommandType.SSOLOGIN2 &&
          token.getCommand() != TokenProto.token.CommandType.SSOLOGIN3) {
        fail(req, resp, "Token Valid, but not a SSOLOGIN token.", locale);
        return;
      }
      long offset = System.currentTimeMillis() - token.getTs();
      offset /= 1000;  // Convert to seconds
      if (offset > 120) {       // Two minutes
        fail(req, resp, "Token Expired. Was valid until " +
          new Date(token.getTs()), locale);
        return;
      }
      // At this point we have a valid SSOLOGIN token

      userInfo = new OdeAuthFilter.UserInfo();
      if (token.getCommand() == TokenProto.token.CommandType.SSOLOGIN) {
        userInfo.setUserId(token.getUuid());
      } else if (token.getCommand() == TokenProto.token.CommandType.SSOLOGIN2) { // SSOLOGIN2
        String email = token.getName();
        if (email == null || email.isEmpty()) {
          fail(req, resp, "Failed to provide an Email Address for login.", locale);
          return;
        }
        User user = storageIo.getUserFromEmail(email);
        userInfo.setUserId(user.getUserId());
      } else {                  // SSOLOGIN3
        String uuid = token.getUuid();
        String email = token.getName();
        if (email == null || email.isEmpty() || uuid == null || uuid.isEmpty()) {
          fail(req, resp, "Failed to provide email and uuid, shouldn't happen!", locale);
          return;
        }
        User user = storageIo.getUser(uuid, email);
        userInfo.setUserId(user.getUserId());
        for (LoginListener listener : loginListeners) {
          listener.onLogin(user, token);
        }
      }

      userInfo.setReadOnly(token.getReadOnly());

      // Check to see if this is a one project token
      long oneProjectId = token.getOneProjectId();
      LOG.log(Level.INFO, "oneProjectId = " + oneProjectId);
      if (oneProjectId != 0) {  // It is...
        try {
          userInfo.setUserId(storageIo.getProjectUserId(oneProjectId));
          userInfo.setOneProjectId(oneProjectId);
        } catch (ProjectNotFoundException e) {
          fail(req, resp, e.getMessage(), locale);
        }
      }

      userInfo.setFauxProjectName(token.getDisplayprojectname());

      String fauxUserName = token.getDisplayaccountname();

      userInfo.setFauxAccountName(fauxUserName);

      String newCookie = userInfo.buildCookie(false);
      if (newCookie != null) {
        Cookie cook = new Cookie("AppInventor", newCookie);
        cook.setPath("/");
        resp.addCookie(cook);
      }

      String uri = new UriBuilder("/")
        .add("locale", locale)
        .add("repo", repo)
        .add("ng", newGalleryId)
        .add("galleryId", galleryId)
        .add("autoload", autoload)
        .add("ui", uiPreference)
        .add("redirect", redirect).build();
      resp.sendRedirect(uri);   // This should bring up App Inventor
      return;
    }

    String emailAddress = bundle.getString("emailaddress");
    String password = bundle.getString("password");
    String login = bundle.getString("login");
    String passwordclickhere = bundle.getString("passwordclickhere");

    req.setCharacterEncoding("UTF-8");
    if (useGoogle.get()) {
      req.setAttribute("useGoogleLabel", "true");
    } else {
      req.setAttribute("useGoogleLabel", "false");
    }
    req.setAttribute("emailAddressLabel", emailAddress);
    req.setAttribute("passwordLabel", password);
    req.setAttribute("loginLabel", login);
    req.setAttribute("passwordclickhereLabel", passwordclickhere);
    req.setAttribute("localeLabel", locale);
    req.setAttribute("pleaselogin", bundle.getString("pleaselogin"));
    req.setAttribute("login", bundle.getString("login"));
    req.setAttribute("autoload", autoload);
    req.setAttribute("repo", repo);
    req.setAttribute("locale", locale);
    req.setAttribute("ng", newGalleryId);
    req.setAttribute("ui", uiPreference);
    req.setAttribute("galleryId", galleryId);
    req.setAttribute("status", params.get("status"));
    req.setAttribute("view", params.get("view"));
    req.setAttribute("email", params.get("email"));
    req.setAttribute("firebaseApiKey", firebaseApiKey.get());
    req.setAttribute("firebaseAuthDomain", firebaseAuthDomain.get());
    req.setAttribute("firebaseProjectId", firebaseProjectId.get());
    req.setAttribute("firebaseAppId", firebaseAppId.get());
    try {
      req.getRequestDispatcher("/login.jsp").forward(req, resp);
    } catch (ServletException e) {
      throw new IOException(e);
    }
  }

  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    BufferedReader input = new BufferedReader(new InputStreamReader(req.getInputStream()));
    String queryString = input.readLine();

    PrintWriter out;

    OdeAuthFilter.UserInfo userInfo = OdeAuthFilter.getUserInfo(req);

    if (userInfo == null) {
      userInfo = new OdeAuthFilter.UserInfo();
    }

    if (queryString == null) {
      out = setCookieOutput(userInfo, resp);
      out.println("queryString is null");
      return;
    }

    HashMap<String, String> params = getQueryMap(queryString);
    String page = getPage(req);
    String action = params.get("action");
    String locale = params.get("locale");
    String repo = params.get("repo");
    String galleryId = params.get("galleryId");
    String newGalleryId = params.get("ng");
    String redirect = params.get("redirect");
    String autoload = params.get("autoload");
    String uiPreference = params.get("ui");

    if (locale == null) {
      locale = "en";
    }

    ResourceBundle bundle = ResourceBundle.getBundle("com/google/appinventor/server/loginmessages", new Locale(locale));

    if (DEBUG) {
      LOG.info("locale = " + locale + " bundle: " + new Locale(locale));
    }

    // Handle Forgot Password Request
    if (page.equals("sendlink") || "forgotPassword".equalsIgnoreCase(action)) {
      String email = params.get("email");
      if (email == null || email.trim().isEmpty()) {
        fail(req, resp, "No Email Address Provided", locale);
        return;
      }
      email = email.trim();

      // 1. Dispatch Firebase password reset email
      sendPasswordResetFirebase(email);

      // 2. Also create local reset link if Datastore/local mail configured
      PWData pwData = storageIo.createPWData(email);
      if (pwData != null) {
        String link = trimPage(req) + pwData.id + "/setpw";
        sendmail(email, link, locale);
        storageIo.cleanuppwdata();
      }

      String isAjax = params.get("isAjax");
      if ("true".equalsIgnoreCase(isAjax)) {
        resp.setContentType("application/json; charset=utf-8");
        PrintWriter pw = resp.getWriter();
        pw.print("{\"success\":true,\"message\":\"Password reset link sent\"}");
        pw.flush();
        return;
      }

      String uri = new UriBuilder("/login")
        .add("status", "linksent")
        .add("email", email)
        .add("locale", locale)
        .add("repo", repo)
        .add("autoload", autoload)
        .add("ng", newGalleryId)
        .add("ui", uiPreference)
        .add("galleryId", galleryId)
        .add("redirect", redirect).build();
      resp.sendRedirect(uri);
      return;
    }

    // Handle Email Verification Resend Request
    if ("sendVerification".equalsIgnoreCase(action) || "resendVerification".equalsIgnoreCase(action)) {
      String idToken = params.get("firebaseToken");
      String email = params.get("email");
      boolean sent = false;
      if (idToken != null && !idToken.trim().isEmpty()) {
        sent = sendEmailVerificationFirebase(idToken);
      }
      String isAjax = params.get("isAjax");
      if ("true".equalsIgnoreCase(isAjax)) {
        resp.setContentType("application/json; charset=utf-8");
        PrintWriter pw = resp.getWriter();
        pw.print("{\"success\":" + sent + ",\"message\":\"" + (sent ? "Verification email sent" : "Failed to send verification email") + "\"}");
        pw.flush();
        return;
      }
      String uri = new UriBuilder("/login")
        .add("status", "verification_sent")
        .add("email", email)
        .add("locale", locale)
        .add("repo", repo)
        .add("autoload", autoload)
        .add("ng", newGalleryId)
        .add("ui", uiPreference)
        .add("galleryId", galleryId)
        .add("redirect", redirect).build();
      resp.sendRedirect(uri);
      return;
    }

    // Handle Set/Reset Password Submission
    if (page.equals("setpw")) {
      String uid = getParam(req);
      if (uid == null || uid.isEmpty()) {
        uid = params.get("uid");
      }
      User user = null;
      if (uid != null && !uid.isEmpty()) {
        PWData data = storageIo.findPWData(uid);
        if (data != null) {
          user = storageIo.getUserFromEmail(data.email);
        }
      }
      if (user == null && userInfo != null && !userInfo.getUserId().equals("")) {
        user = storageIo.getUser(userInfo.getUserId());
      }
      if (user == null) {
        fail(req, resp, "Session Timed Out or Invalid Reset Token", locale);
        return;
      }
      String password = params.get("password");
      if (password == null || password.equals("")) {
        fail(req, resp, bundle.getString("nopassword"), locale);
        return;
      }
      String hashedPassword;
      try {
        hashedPassword = PasswordHash.createHash(password);
      } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
        fail(req, resp, "System Error hashing password", locale);
        return;
      }

      storageIo.setUserPassword(user.getUserId(), hashedPassword);
      storageIo.cleanuppwdata();

      String uri = new UriBuilder("/login")
        .add("status", "password_updated")
        .add("locale", locale)
        .add("repo", repo)
        .add("autoload", autoload)
        .add("ng", newGalleryId)
        .add("ui", uiPreference)
        .add("galleryId", galleryId)
        .add("redirect", redirect).build();
      resp.sendRedirect(uri);
      return;
    }

    String firebaseToken = params.get("firebaseToken");
    String email = params.get("email");
    String password = params.get("password");
    boolean isRegister = "true".equalsIgnoreCase(params.get("isRegister"));

    String verifiedEmail = null;
    String verifiedUid = null;
    boolean emailVerified = false;

    if (firebaseToken != null && !firebaseToken.trim().isEmpty()) {
      VerifiedUser vUser = verifyFirebaseIdToken(firebaseToken);
      if (vUser != null) {
        verifiedEmail = vUser.email;
        verifiedUid = vUser.uid;
        emailVerified = vUser.emailVerified;
      }
    }

    if (verifiedEmail == null && email != null && !email.trim().isEmpty()) {
      if (password != null && !password.trim().isEmpty()) {
        if (isRegister) {
          VerifiedUser vUser = signUpFirebase(email, password);
          if (vUser != null) {
            verifiedEmail = vUser.email != null ? vUser.email : email;
            verifiedUid = vUser.uid;
            emailVerified = vUser.emailVerified;
          }
        } else {
          VerifiedUser vUser = signInFirebase(email, password);
          if (vUser != null) {
            verifiedEmail = vUser.email != null ? vUser.email : email;
            verifiedUid = vUser.uid;
            emailVerified = vUser.emailVerified;
          }
        }
      }
    }

    // Fallback if client posted email with firebaseToken
    if (verifiedEmail == null && email != null && !email.trim().isEmpty() && firebaseToken != null && !firebaseToken.trim().isEmpty()) {
      verifiedEmail = email;
    }

    // Fallback check against local password hash if Firebase offline/unconfigured
    if (verifiedEmail == null && email != null && !email.trim().isEmpty()) {
      User tempUser = storageIo.getUserFromEmail(email);
      String hash = tempUser != null ? tempUser.getPassword() : null;
      if (hash != null && !hash.isEmpty() && password != null) {
        try {
          if (PasswordHash.validatePassword(password, hash)) {
            verifiedEmail = email;
            verifiedUid = tempUser.getUserId();
            emailVerified = true;
          }
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ignored) {
        }
      }
    }

    if (verifiedEmail == null || verifiedEmail.trim().isEmpty()) {
      fail(req, resp, bundle.getString("invalidpassword"), locale);
      return;
    }

    // Require email verification before allowing access
    if (isRegister || !emailVerified) {
      String uri = new UriBuilder("/login")
        .add("view", "verify")
        .add("status", "verification_sent")
        .add("email", verifiedEmail)
        .add("locale", locale)
        .add("repo", repo)
        .add("autoload", autoload)
        .add("ng", newGalleryId)
        .add("ui", uiPreference)
        .add("galleryId", galleryId)
        .add("redirect", redirect).build();
      resp.sendRedirect(uri);
      return;
    }

    // Look up existing user or register new user in Datastore
    User user = storageIo.getUserFromEmail(verifiedEmail);
    if (user == null) {
      String uidToUse = (verifiedUid != null && !verifiedUid.isEmpty()) ? verifiedUid : verifiedEmail;
      user = storageIo.getUser(uidToUse, verifiedEmail);
    }

    if (user == null) {
      fail(req, resp, "Failed to create user session", locale);
      return;
    }

    userInfo.setUserId(user.getUserId());
    userInfo.setIsAdmin(user.getIsAdmin());
    String newCookie = userInfo.buildCookie(false);
    if (DEBUG) {
      LOG.info("newCookie = " + newCookie);
    }
    if (newCookie != null) {
      Cookie cook = new Cookie("AppInventor", newCookie);
      cook.setPath("/");
      resp.addCookie(cook);
    }

    String uri = "/";
    if (redirect != null && !redirect.equals("")) {
      uri = redirect;
    }
    uri = new UriBuilder(uri)
      .add("locale", locale)
      .add("autoload", autoload)
      .add("repo", repo)
      .add("ng", newGalleryId)
      .add("ui", uiPreference)
      .add("galleryId", galleryId).build();
    resp.sendRedirect(uri);
    return;
  }

  private static class VerifiedUser {
    final String uid;
    final String email;
    final boolean emailVerified;

    VerifiedUser(String uid, String email, boolean emailVerified) {
      this.uid = uid;
      this.email = email;
      this.emailVerified = emailVerified;
    }
  }

  /**
   * Verifies a Firebase ID token using the Firebase Identity Toolkit lookup REST API.
   */
  private static VerifiedUser verifyFirebaseIdToken(String idToken) {
    if (idToken == null || idToken.trim().isEmpty()) {
      return null;
    }
    String apiKey = Flag.createFlag("firebase.api.key", "AIzaSyCNMBDXRM7cCJHpoTkz8xPJ_yCRmn2LP4Q").get();
    try {
      URL url = new URL("https://identitytoolkit.googleapis.com/v1/accounts:lookup?key=" + apiKey);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("POST");
      conn.setRequestProperty("Content-Type", "application/json");
      conn.setDoOutput(true);

      String jsonInputString = "{\"idToken\":\"" + idToken.trim() + "\"}";
      try (OutputStream os = conn.getOutputStream()) {
        byte[] input = jsonInputString.getBytes("utf-8");
        os.write(input, 0, input.length);
      }

      if (conn.getResponseCode() == 200) {
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
          String responseLine;
          while ((responseLine = br.readLine()) != null) {
            response.append(responseLine.trim());
          }
        }
        String json = response.toString();
        String uid = parseJsonField(json, "localId");
        String email = parseJsonField(json, "email");
        boolean emailVerified = parseJsonBooleanField(json, "emailVerified");
        return new VerifiedUser(uid, email, emailVerified);
      } else {
        LOG.warning("Firebase ID Token verification returned HTTP " + conn.getResponseCode());
      }
    } catch (Exception e) {
      LOG.log(Level.WARNING, "Error verifying Firebase ID Token", e);
    }
    return null;
  }

  /**
   * Dispatches a password reset email via Firebase Identity Toolkit.
   */
  public static boolean sendPasswordResetFirebase(String email) {
    if (email == null || email.trim().isEmpty()) return false;
    String apiKey = Flag.createFlag("firebase.api.key", "AIzaSyCNMBDXRM7cCJHpoTkz8xPJ_yCRmn2LP4Q").get();
    try {
      URL url = new URL("https://identitytoolkit.googleapis.com/v1/accounts:sendOobCode?key=" + apiKey);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("POST");
      conn.setRequestProperty("Content-Type", "application/json");
      conn.setDoOutput(true);

      String jsonInputString = "{\"requestType\":\"PASSWORD_RESET\",\"email\":\"" + email.trim() + "\"}";
      try (OutputStream os = conn.getOutputStream()) {
        byte[] input = jsonInputString.getBytes("utf-8");
        os.write(input, 0, input.length);
      }

      int responseCode = conn.getResponseCode();
      if (responseCode == 200) {
        LOG.info("Firebase password reset email successfully sent to: " + email);
        return true;
      } else {
        LOG.warning("Firebase sendOobCode (PASSWORD_RESET) returned HTTP " + responseCode);
      }
    } catch (Exception e) {
      LOG.log(Level.WARNING, "Error sending Firebase password reset email", e);
    }
    return false;
  }

  /**
   * Dispatches an email verification request via Firebase Identity Toolkit.
   */
  public static boolean sendEmailVerificationFirebase(String idToken) {
    if (idToken == null || idToken.trim().isEmpty()) return false;
    String apiKey = Flag.createFlag("firebase.api.key", "AIzaSyCNMBDXRM7cCJHpoTkz8xPJ_yCRmn2LP4Q").get();
    try {
      URL url = new URL("https://identitytoolkit.googleapis.com/v1/accounts:sendOobCode?key=" + apiKey);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("POST");
      conn.setRequestProperty("Content-Type", "application/json");
      conn.setDoOutput(true);

      String jsonInputString = "{\"requestType\":\"VERIFY_EMAIL\",\"idToken\":\"" + idToken.trim() + "\"}";
      try (OutputStream os = conn.getOutputStream()) {
        byte[] input = jsonInputString.getBytes("utf-8");
        os.write(input, 0, input.length);
      }

      int responseCode = conn.getResponseCode();
      if (responseCode == 200) {
        LOG.info("Firebase email verification successfully dispatched.");
        return true;
      } else {
        LOG.warning("Firebase sendOobCode (VERIFY_EMAIL) returned HTTP " + responseCode);
      }
    } catch (Exception e) {
      LOG.log(Level.WARNING, "Error sending Firebase email verification", e);
    }
    return false;
  }

  private static VerifiedUser signInFirebase(String email, String password) {
    if (email == null || password == null) return null;
    String apiKey = Flag.createFlag("firebase.api.key", "AIzaSyCNMBDXRM7cCJHpoTkz8xPJ_yCRmn2LP4Q").get();
    try {
      URL url = new URL("https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + apiKey);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("POST");
      conn.setRequestProperty("Content-Type", "application/json");
      conn.setDoOutput(true);

      String jsonInputString = "{\"email\":\"" + email + "\",\"password\":\"" + password + "\",\"returnSecureToken\":true}";
      try (OutputStream os = conn.getOutputStream()) {
        byte[] input = jsonInputString.getBytes("utf-8");
        os.write(input, 0, input.length);
      }

      if (conn.getResponseCode() == 200) {
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
          String responseLine;
          while ((responseLine = br.readLine()) != null) {
            response.append(responseLine.trim());
          }
        }
        String json = response.toString();
        String uid = parseJsonField(json, "localId");
        String retEmail = parseJsonField(json, "email");
        boolean emailVerified = parseJsonBooleanField(json, "emailVerified");
        return new VerifiedUser(uid, retEmail != null ? retEmail : email, emailVerified);
      }
    } catch (Exception e) {
      LOG.log(Level.WARNING, "Firebase signInWithPassword REST error", e);
    }
    return null;
  }

  private static VerifiedUser signUpFirebase(String email, String password) {
    if (email == null || password == null) return null;
    String apiKey = Flag.createFlag("firebase.api.key", "AIzaSyCNMBDXRM7cCJHpoTkz8xPJ_yCRmn2LP4Q").get();
    try {
      URL url = new URL("https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=" + apiKey);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("POST");
      conn.setRequestProperty("Content-Type", "application/json");
      conn.setDoOutput(true);

      String jsonInputString = "{\"email\":\"" + email + "\",\"password\":\"" + password + "\",\"returnSecureToken\":true}";
      try (OutputStream os = conn.getOutputStream()) {
        byte[] input = jsonInputString.getBytes("utf-8");
        os.write(input, 0, input.length);
      }

      if (conn.getResponseCode() == 200) {
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
          String responseLine;
          while ((responseLine = br.readLine()) != null) {
            response.append(responseLine.trim());
          }
        }
        String json = response.toString();
        String uid = parseJsonField(json, "localId");
        String retEmail = parseJsonField(json, "email");
        String idToken = parseJsonField(json, "idToken");
        boolean emailVerified = parseJsonBooleanField(json, "emailVerified");
        if (idToken != null && !idToken.isEmpty()) {
          sendEmailVerificationFirebase(idToken);
        }
        return new VerifiedUser(uid, retEmail != null ? retEmail : email, emailVerified);
      }
    } catch (Exception e) {
      LOG.log(Level.WARNING, "Firebase signUp REST error", e);
    }
    return null;
  }

  private static boolean parseJsonBooleanField(String json, String field) {
    if (json == null || field == null) return false;
    String pattern = "\"" + field + "\"\\s*:\\s*(true|false)";
    java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern, java.util.regex.Pattern.CASE_INSENSITIVE);
    java.util.regex.Matcher m = p.matcher(json);
    if (m.find()) {
      return Boolean.parseBoolean(m.group(1));
    }
    return false;
  }

  private static String parseJsonField(String json, String field) {
    if (json == null || field == null) return null;
    String pattern = "\"" + field + "\"\\s*:\\s*\"([^\"]+)\"";
    java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
    java.util.regex.Matcher m = p.matcher(json);
    if (m.find()) {
      return m.group(1);
    }
    return null;
  }

  public void destroy() {
    super.destroy();
  }

  private static HashMap<String, String> getQueryMap(String query)  {
    HashMap<String, String> map = new HashMap<String, String>();
    if (query == null || query.equals("")) {
      return map;               // Empty map
    }
    String[] params = query.split("&");
    for (String param : params)  {
      String [] nvpair = param.split("=");
      if (nvpair.length <= 1) {
        map.put(nvpair[0], "");
      } else
        map.put(nvpair[0], URLDecoder.decode(nvpair[1]));
    }
    return map;
  }

  // Note: Urls in this servlet are of the form /login/<param>/<page>
  // The page identifier is *after* the parameter, if there is one.

  private String getPage(HttpServletRequest req) {
    String [] components = req.getRequestURI().split("/");
    return components[components.length-1];
  }

  private String getParam(HttpServletRequest req) {
    String [] components = req.getRequestURI().split("/");
    if (components.length < 2)
      return null;
    return components[components.length-2];
  }

  private String trimPage(HttpServletRequest req) {
    String [] components = req.getRequestURL().toString().split("/");
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < components.length-1; i++)
      sb.append(components[i] + "/");
    return sb.toString();
  }

  private void fail(HttpServletRequest req, HttpServletResponse resp, String error, String locale) throws IOException {
    resp.sendRedirect("/login/?locale=" + sanitizer.sanitize(locale) + "&error=" + sanitizer.sanitize(error));
    return;
  }

  private void sendmail(String email, String url, String locale) {
    try {
      String tmailServer = mailServer.get();
      if (tmailServer.equals("")) { // No mailserver = no mail!
        return;
      }
      URL mailServerUrl = new URL(tmailServer);
      HttpURLConnection connection = (HttpURLConnection) mailServerUrl.openConnection();
      connection.setDoOutput(true);
      connection.setRequestMethod("POST");
      PrintWriter stream = new PrintWriter(connection.getOutputStream());
      stream.write("email=" + URLEncoder.encode(email) + "&url=" + URLEncoder.encode(url) +
          "&pass=" + password.get() + "&locale=" + locale);
      stream.flush();
      stream.close();
      int responseCode = 0;
      responseCode = connection.getResponseCode();
      if (responseCode != HttpURLConnection.HTTP_OK) {
        LOG.warning("mailserver responded with code = " + responseCode);
        // Nothing else we can do here...
      }
    } catch (MalformedURLException e) {
    } catch (IOException e) {
    }
  }

  private PrintWriter setCookieOutput(OdeAuthFilter.UserInfo userInfo, HttpServletResponse resp)
    throws IOException {
    if (userInfo != null) {     // if we never had logged in, this will be null!
      String newCookie = userInfo.buildCookie(true);
      if (newCookie != null) {
        Cookie cook = new Cookie("AppInventor", newCookie);
        cook.setPath("/");
        resp.addCookie(cook);
      }
    }
    resp.setContentType("text/html; charset=utf-8");
    PrintWriter out = resp.getWriter();
    return out;
  }

}
