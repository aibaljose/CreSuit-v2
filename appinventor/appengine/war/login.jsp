<%@page import="javax.servlet.http.HttpServletRequest"%>
<%@page import="com.google.appinventor.server.util.UriBuilder"%>
<%@page import="org.apache.commons.lang3.StringEscapeUtils"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!doctype html>
<%
   String error = StringEscapeUtils.escapeHtml4(request.getParameter("error"));
   String status = StringEscapeUtils.escapeHtml4(request.getParameter("status"));
   if (status == null) {
       status = StringEscapeUtils.escapeHtml4((String) request.getAttribute("status"));
   }
   String view = StringEscapeUtils.escapeHtml4(request.getParameter("view"));
   if (view == null) {
       view = StringEscapeUtils.escapeHtml4((String) request.getAttribute("view"));
   }
   if (view == null || view.isEmpty()) {
       view = "login";
   }

   String emailParam = StringEscapeUtils.escapeHtml4(request.getParameter("email"));
   if (emailParam == null) {
       emailParam = StringEscapeUtils.escapeHtml4((String) request.getAttribute("email"));
   }
   if (emailParam == null) {
       emailParam = "";
   }

   String resetUid = StringEscapeUtils.escapeHtml4((String) request.getAttribute("resetUid"));
   String resetEmail = StringEscapeUtils.escapeHtml4((String) request.getAttribute("resetEmail"));

   String useGoogleLabel = (String) request.getAttribute("useGoogleLabel");
   String firebaseApiKey = (String) request.getAttribute("firebaseApiKey");
   if (firebaseApiKey == null || firebaseApiKey.isEmpty()) {
       firebaseApiKey = "AIzaSyCNMBDXRM7cCJHpoTkz8xPJ_yCRmn2LP4Q";
   }
   String firebaseAuthDomain = (String) request.getAttribute("firebaseAuthDomain");
   String firebaseProjectId = (String) request.getAttribute("firebaseProjectId");
   String firebaseAppId = (String) request.getAttribute("firebaseAppId");
   String locale = StringEscapeUtils.escapeHtml4(request.getParameter("locale"));
   String redirect = StringEscapeUtils.escapeHtml4(request.getParameter("redirect"));
   String repo = StringEscapeUtils.escapeHtml4((String) request.getAttribute("repo"));
   String autoload = StringEscapeUtils.escapeHtml4((String) request.getAttribute("autoload"));
   String galleryId = StringEscapeUtils.escapeHtml4((String) request.getAttribute("galleryId"));
   String newGalleryId = StringEscapeUtils.escapeHtml4(request.getParameter("ng"));
   String uiPreference = StringEscapeUtils.escapeHtml4(request.getParameter("ui"));
   if (locale == null) {
       locale = "en";
   }
%>
<html class="h-full">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <meta HTTP-EQUIV="pragma" CONTENT="no-cache"/>
    <meta HTTP-EQUIV="Cache-Control" CONTENT="no-cache, must-revalidate"/>
    <meta HTTP-EQUIV="expires" CONTENT="0"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>CreSuit - Authentication</title>
    
    <!-- Tailwind CSS v3 CDN -->
    <script src="https://cdn.tailwindcss.com"></script>
    
    <!-- Google Fonts Inter -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
    
    <!-- Firebase Web SDK (Compat mode) -->
    <script src="https://www.gstatic.com/firebasejs/10.8.0/firebase-app-compat.js"></script>
    <script src="https://www.gstatic.com/firebasejs/10.8.0/firebase-auth-compat.js"></script>

    <style>
      body {
        font-family: 'Inter', system-ui, -apple-system, sans-serif;
        background-color: #edf0f7;
      }
      .fade-in {
        animation: fadeIn 0.25s cubic-bezier(0.16, 1, 0.3, 1) forwards;
      }
      @keyframes fadeIn {
        from { opacity: 0; transform: translateY(6px); }
        to { opacity: 1; transform: translateY(0); }
      }
    </style>
  </head>
  <body class="min-h-screen bg-[#edf0f7] flex items-center justify-center p-3 sm:p-6 md:p-8 antialiased text-slate-800">
    
    <!-- Outer Card Shell -->
    <div class="w-full max-w-4xl bg-[#edf0f7] rounded-[24px] sm:rounded-[32px] border border-slate-200/60 p-2.5 sm:p-3 md:p-4 relative overflow-hidden shadow-[0_20px_60px_-15px_rgba(0,0,0,0.07)]">
      
      <div class="grid grid-cols-1 lg:grid-cols-12 gap-4 lg:gap-6 items-stretch">
        
        <!-- Left Panel: Form & Branding -->
        <div class="lg:col-span-6 flex flex-col justify-between p-3 sm:p-6 lg:p-7 min-h-[520px]">
          
          <!-- Header Logo / Brand -->
          <div class="flex items-center justify-between mb-4">
            <div class="flex items-center space-x-2.5 cursor-pointer" onclick="switchView('login')">
              <div class="w-8 h-8 rounded-full bg-[#3557ff] flex items-center justify-center shadow-sm shadow-blue-500/20">
                <!-- Geometric Flower/Snowflake SVG Logo -->
                <svg class="w-4 h-4 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
                  <circle cx="12" cy="12" r="3"/>
                  <path d="M12 3v3M12 18v3M3 12h3M18 12h3M5.6 5.6l2.1 2.1M16.3 16.3l2.1 2.1M5.6 18.4l2.1-2.1M16.3 7.7l2.1-2.1"/>
                </svg>
              </div>
              <span class="text-lg font-bold text-slate-900 tracking-tight">CreSuit</span>
            </div>

            <!-- View Badge -->
            <span id="view-badge" class="text-[10px] font-bold uppercase tracking-wider text-blue-600 bg-blue-50 border border-blue-200/60 px-2 py-0.5 rounded-full">
              Sign In
            </span>
          </div>

          <!-- Main Interactive Container -->
          <div class="w-full max-w-sm mx-auto my-auto py-1">
            
            <!-- Global Dynamic Alert Banner -->
            <div id="dynamic-alert" class="hidden mb-4 p-3 rounded-xl border-l-4 shadow-xs transition duration-200">
              <div class="flex items-start">
                <svg id="dynamic-alert-icon" class="h-4 w-4 mr-2 shrink-0 mt-0.5" viewBox="0 0 20 20" fill="currentColor">
                  <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clip-rule="evenodd" />
                </svg>
                <p id="dynamic-alert-msg" class="text-xs font-semibold leading-snug"></p>
              </div>
            </div>

            <% if (error != null && !error.isEmpty()) { %>
            <div class="mb-4 bg-rose-50 border-l-4 border-rose-500 p-3 rounded-r-xl shadow-xs">
              <div class="flex items-start">
                <svg class="h-4 w-4 text-rose-500 mr-2 shrink-0 mt-0.5" viewBox="0 0 20 20" fill="currentColor">
                  <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clip-rule="evenodd" />
                </svg>
                <p class="text-xs font-semibold text-rose-800"><%= error %></p>
              </div>
            </div>
            <% } %>

            <% if ("linksent".equals(status)) { %>
            <div class="mb-4 bg-emerald-50 border-l-4 border-emerald-500 p-3 rounded-r-xl shadow-xs">
              <div class="flex items-start">
                <svg class="h-4 w-4 text-emerald-500 mr-2 shrink-0 mt-0.5" viewBox="0 0 20 20" fill="currentColor">
                  <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
                </svg>
                <p class="text-xs font-semibold text-emerald-800">Password reset link sent! Check your inbox (and spam folder).</p>
              </div>
            </div>
            <% } else if ("verification_sent".equals(status)) { %>
            <div class="mb-4 bg-blue-50 border-l-4 border-blue-500 p-3 rounded-r-xl shadow-xs">
              <div class="flex items-start">
                <svg class="h-4 w-4 text-blue-500 mr-2 shrink-0 mt-0.5" viewBox="0 0 20 20" fill="currentColor">
                  <path d="M2.003 5.884L10 9.882l7.997-3.998A2 2 0 0016 4H4a2 2 0 00-1.997 1.884z" />
                  <path d="M18 8.118l-8 4-8-4V14a2 2 0 002 2h12a2 2 0 002-2V8.118z" />
                </svg>
                <p class="text-xs font-semibold text-blue-800">Email verification sent! Please verify your email address.</p>
              </div>
            </div>
            <% } else if ("password_updated".equals(status)) { %>
            <div class="mb-4 bg-emerald-50 border-l-4 border-emerald-500 p-3 rounded-r-xl shadow-xs">
              <div class="flex items-start">
                <svg class="h-4 w-4 text-emerald-500 mr-2 shrink-0 mt-0.5" viewBox="0 0 20 20" fill="currentColor">
                  <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
                </svg>
                <p class="text-xs font-semibold text-emerald-800">Password updated successfully. You can now log in.</p>
              </div>
            </div>
            <% } %>

            <!-- ================= VIEW 1 & 2: LOGIN & REGISTER FORMS ================= -->
            <div id="auth-view-container" class="fade-in <%= ("forgot".equals(view) || "verify".equals(view) || "setpw".equals(view)) ? "hidden" : "" %>">
              
              <!-- Heading -->
              <div class="mb-4">
                <h1 id="form-heading" class="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight leading-tight">Welcome Back</h1>
                <p id="form-subtitle" class="text-slate-400 text-xs mt-1 font-normal">Enter your email and password to access your account.</p>
              </div>

              <!-- Credentials Form -->
              <form id="loginForm" method="POST" action="/login" class="space-y-3">
                
                <!-- Email Field -->
                <div>
                  <label for="email" class="block text-[11px] font-semibold text-slate-700 mb-1">
                    Email
                  </label>
                  <input id="email" name="email" type="email" autocomplete="email" required 
                         value="<%= emailParam %>"
                         placeholder="you@company.com"
                         class="w-full px-3.5 py-2.5 rounded-xl border border-slate-200 bg-white text-xs text-slate-900 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-[#3557ff]/20 focus:border-[#3557ff] transition duration-150 shadow-xs">
                </div>

                <!-- Password Field -->
                <div>
                  <label for="password" class="block text-[11px] font-semibold text-slate-700 mb-1">
                    Password
                  </label>
                  <div class="relative">
                    <input id="password" name="password" type="password" autocomplete="current-password" required 
                           placeholder="••••••••"
                           class="w-full px-3.5 py-2.5 pr-10 rounded-xl border border-slate-200 bg-white text-xs text-slate-900 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-[#3557ff]/20 focus:border-[#3557ff] transition duration-150 shadow-xs">
                    
                    <button type="button" onclick="togglePasswordVisibility('password', 'eye-icon-on', 'eye-icon-off')" class="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 focus:outline-none p-1">
                      <svg id="eye-icon-off" class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858-5.908a10.04 10.04 0 013.122-.463c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m-4.692-4.692a3 3 0 00-4.243-4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l18 18" />
                      </svg>
                      <svg id="eye-icon-on" class="w-3.5 h-3.5 hidden" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                        <path stroke-linecap="round" stroke-linejoin="round" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                      </svg>
                    </button>
                  </div>
                </div>

                <!-- Remember Me & Forgot Password Row -->
                <div id="login-options-row" class="flex items-center justify-between pt-0.5 pb-0.5">
                  <label class="flex items-center space-x-2 cursor-pointer">
                    <input type="checkbox" id="remember" class="w-3.5 h-3.5 rounded border-slate-300 text-[#3557ff] focus:ring-[#3557ff] cursor-pointer">
                    <span class="text-[11px] font-medium text-slate-500">Remember Me</span>
                  </label>
                  
                  <button type="button" onclick="switchView('forgot')" 
                     class="text-[11px] font-bold text-[#3557ff] hover:text-blue-700 transition cursor-pointer">
                    Forgot Password?
                  </button>
                </div>

                <!-- Register Info Notice -->
                <div id="register-info-notice" class="hidden text-[11px] text-slate-500 bg-blue-50/60 border border-blue-100 p-2.5 rounded-xl">
                  <div class="flex items-center space-x-1.5 text-blue-700 font-semibold mb-0.5">
                    <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                    <span>Email Verification</span>
                  </div>
                  <span>A verification link will be emailed to you upon registration to secure your account.</span>
                </div>

                <!-- Hidden Params & Firebase Auth Token Field -->
                <input type="hidden" id="firebaseToken" name="firebaseToken" value="">
                <input type="hidden" id="isRegister" name="isRegister" value="false">
                <% if (locale != null && !locale.equals("")) { %>
                  <input type="hidden" name="locale" value="<%= locale %>">
                <% } %>
                <% if (repo != null && !repo.equals("")) { %>
                  <input type="hidden" name="repo" value="<%= repo %>">
                <% } %>
                <% if (autoload != null && !autoload.equals("")) { %>
                  <input type="hidden" name="autoload" value="<%= autoload %>">
                <% } %>
                <% if (galleryId != null && !galleryId.equals("")) { %>
                  <input type="hidden" name="galleryId" value="<%= galleryId %>">
                <% } %>
                <% if (newGalleryId != null && !newGalleryId.equals("")) { %>
                  <input type="hidden" name="ng" value="<%= newGalleryId %>">
                <% } %>
                <% if (uiPreference != null && !uiPreference.equals("")) { %>
                  <input type="hidden" name="ui" value="<%= uiPreference %>">
                <% } %>
                <% if (redirect != null && !redirect.equals("")) { %>
                  <input type="hidden" name="redirect" value="<%= redirect %>">
                <% } %>

                <!-- Submit Button -->
                <div class="pt-1">
                  <button type="submit" id="submit-btn" 
                          class="w-full py-2.5 px-4 bg-[#3557ff] hover:bg-[#2848ed] text-white font-bold rounded-xl shadow-md shadow-blue-500/20 transition duration-150 ease-in-out text-xs cursor-pointer active:scale-[0.99] flex items-center justify-center space-x-2">
                    <span id="submit-btn-text">Log In</span>
                    <svg id="submit-spinner" class="animate-spin h-3.5 w-3.5 text-white hidden" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                      <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                      <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                  </button>
                </div>
              </form>

              <!-- Or Login With Divider -->
              <div id="social-divider" class="relative flex items-center justify-center my-3.5">
                <div class="absolute inset-0 flex items-center" aria-hidden="true">
                  <div class="w-full border-t border-slate-200"></div>
                </div>
                <div class="relative bg-[#edf0f7] px-2.5 text-[11px] font-medium text-slate-400">
                  Or Continue With
                </div>
              </div>

              <!-- Social Login Buttons -->
              <div id="social-buttons-grid" class="grid grid-cols-2 gap-2">
                <!-- Google Login Button -->
                <button type="button" onclick="loginWithGoogle()" class="flex items-center justify-center gap-1.5 py-2 px-2 rounded-xl border border-slate-200 bg-white hover:bg-slate-50 text-[11px] font-bold text-slate-700 shadow-xs transition duration-150 cursor-pointer">
                  <svg class="h-3.5 w-3.5 shrink-0" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                    <path d="M21.35,11.1H12v2.7h5.38C16.88,15.54,14.77,16.5,12,16.5c-3.03,0-5.6-2.05-6.52-4.8c-0.23-0.7-0.37-1.44-0.37-2.2 s0.14-1.5,0.37-2.2c0.92-2.75,3.49-4.8,6.52-4.8c1.65,0,3.13,0.59,4.29,1.71l2.02-2.02C16.51,2.5,14.38,1.5,12,1.5 C7.54,1.5,3.77,4.07,2.02,7.8C1.52,8.87,1.25,10.05,1.25,11.3s0.27,2.43,0.77,3.5c1.75,3.73,5.52,6.3,9.98,6.3 c4.67,0,8.44-3.08,9.75-7.2c0.23-0.7,0.37-1.44,0.37-2.2C22.12,11.45,21.87,11.1,21.35,11.1z" fill="#4285F4"/>
                  </svg>
                  <span>Google</span>
                </button>

                <!-- Apple Login Button -->
                <button type="button" onclick="loginWithFirebaseAuth('apple')" class="flex items-center justify-center gap-1.5 py-2 px-2 rounded-xl border border-slate-200 bg-white hover:bg-slate-50 text-[11px] font-bold text-slate-700 shadow-xs transition duration-150 cursor-pointer">
                  <svg class="h-3.5 w-3.5 text-slate-900 fill-current shrink-0" viewBox="0 0 24 24">
                    <path d="M18.71 19.5c-.83 1.24-1.71 2.45-3.05 2.47-1.34.03-1.77-.79-3.29-.79-1.53 0-2 .77-3.27.82-1.31.05-2.3-1.32-3.14-2.53C4.25 17 2.94 12.45 4.7 9.39c.87-1.52 2.43-2.48 4.12-2.51 1.28-.02 2.5.87 3.29.87.78 0 2.26-1.07 3.81-.91.65.03 2.47.26 3.64 1.98-.09.06-2.17 1.28-2.15 3.81.03 3.02 2.65 4.03 2.68 4.04-.03.07-.42 1.44-1.38 2.83M15.97 6.32c.67-.82 1.13-1.97.99-3.12-1 .04-2.22.67-2.92 1.49-.63.73-1.18 1.91-1.03 3.03 1.12.09 2.28-.58 2.96-1.4z"/>
                  </svg>
                  <span>Apple</span>
                </button>
              </div>

              <!-- Registration / Login Toggle Prompt -->
              <div class="mt-4 text-center">
                <p class="text-[11px] font-semibold text-slate-400">
                  <span id="toggle-prompt-text">Don't Have An Account? </span>
                  <a href="javascript:void(0)" id="toggle-auth-btn" onclick="toggleAuthMode()" class="text-[#3557ff] font-bold hover:underline ml-0.5">Register Now.</a>
                </p>
              </div>

            </div>

            <!-- ================= VIEW 3: FORGOT PASSWORD ================= -->
            <div id="forgot-view-container" class="fade-in <%= "forgot".equals(view) ? "" : "hidden" %>">
              
              <!-- Heading -->
              <div class="mb-4">
                <div class="inline-flex items-center space-x-1.5 text-xs font-semibold text-[#3557ff] mb-1.5 cursor-pointer hover:underline" onclick="switchView('login')">
                  <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
                  </svg>
                  <span>Back to Login</span>
                </div>
                <h1 class="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight leading-tight">Reset Password</h1>
                <p class="text-slate-400 text-xs mt-1 font-normal">Enter your email address and we'll send you a password reset link.</p>
              </div>

              <!-- Forgot Password Form -->
              <form id="forgotForm" method="POST" action="/login" class="space-y-3.5" onsubmit="handleForgotPassword(event)">
                <input type="hidden" name="action" value="forgotPassword">
                <input type="hidden" name="locale" value="<%= locale %>">
                
                <div>
                  <label for="forgot-email" class="block text-[11px] font-semibold text-slate-700 mb-1">
                    Registered Email Address
                  </label>
                  <input id="forgot-email" name="email" type="email" autocomplete="email" required 
                         value="<%= emailParam %>"
                         placeholder="you@company.com"
                         class="w-full px-3.5 py-2.5 rounded-xl border border-slate-200 bg-white text-xs text-slate-900 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-[#3557ff]/20 focus:border-[#3557ff] transition duration-150 shadow-xs">
                </div>

                <div class="pt-1">
                  <button type="submit" id="forgot-submit-btn" 
                          class="w-full py-2.5 px-4 bg-[#3557ff] hover:bg-[#2848ed] text-white font-bold rounded-xl shadow-md shadow-blue-500/20 transition duration-150 ease-in-out text-xs cursor-pointer active:scale-[0.99] flex items-center justify-center space-x-2">
                    <span id="forgot-btn-text">Send Reset Link</span>
                    <svg id="forgot-spinner" class="animate-spin h-3.5 w-3.5 text-white hidden" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                      <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                      <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                  </button>
                </div>
              </form>

              <div class="mt-5 p-3 rounded-xl bg-slate-100/80 border border-slate-200/80 text-[11px] text-slate-500">
                <p class="font-medium">Need immediate help? Ensure you enter the exact email used when creating your account.</p>
              </div>

            </div>

            <!-- ================= VIEW 4: EMAIL VERIFICATION SENT ================= -->
            <div id="verify-view-container" class="fade-in <%= "verify".equals(view) ? "" : "hidden" %>">
              
              <div class="text-center py-2">
                <div class="w-12 h-12 bg-blue-100 text-[#3557ff] rounded-2xl flex items-center justify-center mx-auto mb-3 shadow-xs">
                  <svg class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                  </svg>
                </div>
                
                <h2 class="text-xl sm:text-2xl font-extrabold text-slate-900 tracking-tight">Verify Your Email</h2>
                <p class="text-xs text-slate-500 mt-1.5 leading-relaxed">
                  We've sent a verification link to <br/>
                  <span id="verify-email-display" class="font-bold text-slate-800"><%= emailParam.isEmpty() ? "your email" : emailParam %></span>.
                </p>
                
                <p class="text-[11px] text-slate-400 mt-2">
                  Please click the link in the email to activate your account. Check your spam or junk folder if you don't see it.
                </p>

                <div class="mt-5 space-y-2">
                  <button type="button" id="resend-verification-btn" onclick="resendEmailVerification()"
                          class="w-full py-2.5 px-4 bg-white border border-slate-200 hover:bg-slate-50 text-slate-700 font-bold rounded-xl text-xs transition duration-150 cursor-pointer shadow-xs">
                    Resend Verification Email
                  </button>

                  <button type="button" onclick="switchView('login')" 
                          class="w-full py-2.5 px-4 bg-[#3557ff] hover:bg-[#2848ed] text-white font-bold rounded-xl shadow-md shadow-blue-500/20 text-xs transition duration-150 cursor-pointer">
                    Back to Sign In
                  </button>
                </div>
              </div>

            </div>

            <!-- ================= VIEW 5: SET NEW PASSWORD ================= -->
            <div id="setpw-view-container" class="fade-in <%= "setpw".equals(view) ? "" : "hidden" %>">
              
              <!-- Heading -->
              <div class="mb-4">
                <h1 class="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight leading-tight">Set New Password</h1>
                <p class="text-slate-400 text-xs mt-1 font-normal">
                  <% if (resetEmail != null && !resetEmail.isEmpty()) { %>
                    Setting new password for <span class="font-semibold text-slate-700"><%= resetEmail %></span>
                  <% } else { %>
                    Enter a new secure password for your account.
                  <% } %>
                </p>
              </div>

              <!-- Set Password Form -->
              <form id="setPwForm" method="POST" action="/login/<%= resetUid != null ? resetUid : "" %>/setpw" class="space-y-3.5">
                <input type="hidden" name="uid" value="<%= resetUid != null ? resetUid : "" %>">
                <input type="hidden" name="locale" value="<%= locale %>">
                
                <div>
                  <label for="new-password" class="block text-[11px] font-semibold text-slate-700 mb-1">
                    New Password
                  </label>
                  <div class="relative">
                    <input id="new-password" name="password" type="password" required 
                           placeholder="••••••••"
                           class="w-full px-3.5 py-2.5 pr-10 rounded-xl border border-slate-200 bg-white text-xs text-slate-900 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-[#3557ff]/20 focus:border-[#3557ff] transition duration-150 shadow-xs">
                    
                    <button type="button" onclick="togglePasswordVisibility('new-password', 'setpw-eye-on', 'setpw-eye-off')" class="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 focus:outline-none p-1">
                      <svg id="setpw-eye-off" class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858-5.908a10.04 10.04 0 013.122-.463c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m-4.692-4.692a3 3 0 00-4.243-4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l18 18" />
                      </svg>
                      <svg id="setpw-eye-on" class="w-3.5 h-3.5 hidden" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                        <path stroke-linecap="round" stroke-linejoin="round" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                      </svg>
                    </button>
                  </div>
                </div>

                <div class="pt-1">
                  <button type="submit" 
                          class="w-full py-2.5 px-4 bg-[#3557ff] hover:bg-[#2848ed] text-white font-bold rounded-xl shadow-md shadow-blue-500/20 transition duration-150 ease-in-out text-xs cursor-pointer active:scale-[0.99]">
                    Update Password & Sign In
                  </button>
                </div>
              </form>

            </div>

          </div>

          <!-- Left Footer -->
          <div class="pt-3 border-t border-slate-100 flex items-center justify-between text-[11px] text-slate-400 font-medium">
            <span class="text-slate-400">CreSuit &copy; <%= java.util.Calendar.getInstance().get(java.util.Calendar.YEAR) %></span>
            
            <div class="flex items-center space-x-2.5">
              <!-- Language selector links -->
              <div class="inline-flex items-center space-x-1 text-[10px]">
                <a href="<%= new UriBuilder("/login").add("locale", "zh_CN").add("repo", repo).add("autoload", autoload).add("galleryId", galleryId).add("ui", uiPreference).add("redirect", redirect).build() %>" 
                   class="<%= locale.equals("zh_CN") ? "font-bold text-slate-800" : "hover:text-slate-600" %>">ZH</a>
                <span>·</span>
                <a href="<%= new UriBuilder("/login").add("locale", "pt").add("repo", repo).add("autoload", autoload).add("galleryId", galleryId).add("ui", uiPreference).add("redirect", redirect).build() %>" 
                   class="<%= locale.equals("pt") ? "font-bold text-slate-800" : "hover:text-slate-600" %>">PT</a>
                <span>·</span>
                <a href="<%= new UriBuilder("/login").add("locale", "en").add("repo", repo).add("autoload", autoload).add("galleryId", galleryId).add("ng", newGalleryId).add("ui", uiPreference).add("redirect", redirect).build() %>" 
                   class="<%= locale.equals("en") ? "font-bold text-slate-800" : "hover:text-slate-600" %>">EN</a>
              </div>
            </div>
          </div>

        </div>

        <!-- Right Panel: Blue Hero Card -->
        <div class="lg:col-span-6 bg-[#3557ff] rounded-[20px] sm:rounded-[24px] p-6 sm:p-8 text-white flex flex-col justify-between relative overflow-hidden min-h-[480px] lg:min-h-[520px]">
          
          <!-- Background Abstract Circles & Grid Overlay -->
          <div class="absolute -right-20 -top-20 w-80 h-80 rounded-full border-[30px] border-white/5 pointer-events-none"></div>
          <div class="absolute -left-20 -bottom-20 w-80 h-80 rounded-full border-[40px] border-white/5 pointer-events-none"></div>
          <div class="absolute inset-0 bg-[radial-gradient(ellipse_at_top_right,_var(--tw-gradient-stops))] from-white/10 via-transparent to-transparent pointer-events-none"></div>

          <!-- Hero Heading & Subtitle -->
          <div class="relative z-10 max-w-md">
            <h2 class="text-2xl sm:text-3xl font-bold tracking-tight leading-[1.25]">
              Effortlessly manage your team and operations.
            </h2>
            <p class="text-blue-100/80 text-xs sm:text-sm font-normal mt-2 leading-relaxed">
              Log in to access your CRM dashboard, workflows, and developer tools.
            </p>
          </div>

          <!-- Dashboard Floating Mockup UI -->
          <div class="relative z-10 mt-5 w-full">
            <div class="bg-white/95 backdrop-blur-md rounded-xl p-3 sm:p-4 shadow-[0_20px_50px_-15px_rgba(0,0,0,0.3)] border border-white/30 text-slate-800 transform rotate-1 hover:rotate-0 transition duration-500 ease-out">
              
              <!-- Mockup Top Bar Stats Row -->
              <div class="grid grid-cols-12 gap-2 mb-2.5">
                
                <!-- Stat Box 1: Total Sales -->
                <div class="col-span-4 bg-slate-50/80 p-2 sm:p-2.5 rounded-lg border border-slate-100">
                  <div class="flex items-center justify-between">
                    <span class="text-[9px] font-semibold text-slate-400 uppercase tracking-wider">Total Sales</span>
                    <span class="text-[8px] text-slate-400">...</span>
                  </div>
                  <div class="text-sm sm:text-base font-extrabold text-slate-900 mt-0.5">$189,374</div>
                  <div class="inline-flex items-center text-[8px] font-bold text-violet-600 bg-violet-100/80 px-1 py-0.2 rounded-full mt-1">
                    ↑ 5.7% <span class="text-violet-500 font-normal ml-0.5">from last month</span>
                  </div>
                </div>

                <!-- Stat Box 2: Chat Performance -->
                <div class="col-span-4 bg-slate-50/80 p-2 sm:p-2.5 rounded-lg border border-slate-100 flex flex-col justify-between">
                  <div class="flex items-center justify-between">
                    <span class="text-[9px] font-semibold text-slate-400 uppercase tracking-wider">Performance</span>
                    <span class="text-[8px] text-slate-400">...</span>
                  </div>
                  <div class="text-sm sm:text-base font-extrabold text-slate-900 mt-0.5">99.98%</div>
                  <div class="w-full h-3 mt-0.5">
                    <svg class="w-full h-full text-violet-500 overflow-visible" viewBox="0 0 100 20" fill="none">
                      <path d="M0 15 Q25 0 50 12 T100 5" stroke="currentColor" stroke-width="2.5" fill="none" stroke-linecap="round"/>
                    </svg>
                  </div>
                </div>

                <!-- Stat Box 3: Sales Overview -->
                <div class="col-span-4 bg-slate-50/80 p-2 sm:p-2.5 rounded-lg border border-slate-100">
                  <div class="flex items-center justify-between">
                    <span class="text-[9px] font-semibold text-slate-400 uppercase tracking-wider">Overview</span>
                    <span class="text-[8px] font-medium text-slate-400 bg-white border border-slate-200 px-1 py-0.2 rounded">Weekly ▾</span>
                  </div>
                  <div class="flex items-end gap-1 h-8 mt-1.5">
                    <div class="w-full bg-slate-200 rounded-t h-3"></div>
                    <div class="w-full bg-slate-200 rounded-t h-5"></div>
                    <div class="w-full bg-slate-200 rounded-t h-2.5"></div>
                    <div class="w-full bg-[#3557ff] rounded-t h-7"></div>
                    <div class="w-full bg-slate-200 rounded-t h-4"></div>
                  </div>
                </div>

              </div>

              <!-- Mockup Middle Row (Total Profit & Sales Categories Donut Chart Overlay) -->
              <div class="grid grid-cols-12 gap-2 relative">
                
                <!-- Left: Total Profit Card -->
                <div class="col-span-5 bg-slate-50/80 p-2 sm:p-2.5 rounded-lg border border-slate-100">
                  <div class="flex items-center justify-between">
                    <span class="text-[9px] font-semibold text-slate-400 uppercase tracking-wider">Total Profit</span>
                    <span class="text-[8px] text-slate-400">...</span>
                  </div>
                  <div class="text-sm sm:text-base font-extrabold text-slate-900 mt-0.5">$25,684</div>
                  <div class="inline-flex items-center text-[8px] font-bold text-emerald-600 bg-emerald-100/80 px-1 py-0.2 rounded-full mt-1">
                    ↑ 1.5% <span class="text-emerald-600/80 font-normal ml-0.5">growth</span>
                  </div>
                </div>

                <!-- Right: Floating Donut Chart Box (Overlapping) -->
                <div class="col-span-7 bg-white p-2.5 rounded-lg border border-slate-200/80 shadow-md relative z-20 -mt-4">
                  <div class="flex items-center justify-between mb-1">
                    <div>
                      <h4 class="text-[11px] font-bold text-slate-800">Categories</h4>
                      <p class="text-[8px] text-slate-400">Activity breakdown</p>
                    </div>
                    <span class="text-[8px] font-medium text-slate-400 bg-slate-50 border border-slate-200 px-1 py-0.2 rounded">Monthly ▾</span>
                  </div>
                  
                  <div class="flex items-center justify-between gap-2">
                    <!-- Semi Arc / Donut SVG -->
                    <div class="relative w-14 h-12 flex items-center justify-center">
                      <svg class="w-14 h-14 -rotate-90" viewBox="0 0 36 36">
                        <path stroke-dasharray="70, 100" d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" fill="none" stroke="#3557ff" stroke-width="4" stroke-linecap="round"/>
                        <path stroke-dasharray="20, 100" stroke-dashoffset="-70" d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" fill="none" stroke="#8b5cf6" stroke-width="4" stroke-linecap="round"/>
                      </svg>
                      <div class="absolute text-center mt-1">
                        <span class="block text-[8px] font-extrabold text-slate-900 leading-none">6.2k</span>
                      </div>
                    </div>

                    <!-- Category Legend -->
                    <div class="text-[8px] space-y-0.5 font-medium text-slate-500">
                      <div class="flex items-center justify-between gap-1.5">
                        <span class="flex items-center"><span class="w-1.5 h-1.5 rounded-full bg-[#3557ff] mr-1"></span>Mobile Apps</span>
                        <span class="font-bold text-slate-700">4.4k</span>
                      </div>
                      <div class="flex items-center justify-between gap-1.5">
                        <span class="flex items-center"><span class="w-1.5 h-1.5 rounded-full bg-purple-500 mr-1"></span>Cloud API</span>
                        <span class="font-bold text-slate-700">750</span>
                      </div>
                      <div class="flex items-center justify-between gap-1.5">
                        <span class="flex items-center"><span class="w-1.5 h-1.5 rounded-full bg-violet-300 mr-1"></span>Assets</span>
                        <span class="font-bold text-slate-700">1.1k</span>
                      </div>
                    </div>
                  </div>

                </div>

              </div>

            </div>
          </div>

        </div>

      </div>

    </div>

    <!-- Firebase Auth & UI Scripts -->
    <script>
      // Dynamic Firebase configuration
      const urlParams = new URLSearchParams(window.location.search);
      const customAuthDomain = urlParams.get('authDomain') || window.FIREBASE_AUTH_DOMAIN || '<%= (firebaseAuthDomain != null && !firebaseAuthDomain.isEmpty()) ? firebaseAuthDomain : "" %>';
      const customProjectId = urlParams.get('projectId') || window.FIREBASE_PROJECT_ID || '<%= (firebaseProjectId != null && !firebaseProjectId.isEmpty()) ? firebaseProjectId : "" %>';
      const customApiKey = urlParams.get('apiKey') || window.FIREBASE_API_KEY || '<%= firebaseApiKey %>';

      const defaultFirebaseConfig = window.FIREBASE_CONFIG || {
        apiKey: customApiKey,
        authDomain: customAuthDomain || (customProjectId ? (customProjectId + ".firebaseapp.com") : "cresuit-app.firebaseapp.com"),
        projectId: customProjectId || "cresuit-app",
        storageBucket: (customProjectId || "cresuit-app") + ".appspot.com",
        messagingSenderId: "1234567890",
        appId: '<%= (firebaseAppId != null && !firebaseAppId.isEmpty()) ? firebaseAppId : "1:1234567890:web:abcdef123456" %>'
      };

      try {
        if (!firebase.apps.length) {
          firebase.initializeApp(defaultFirebaseConfig);
        }
      } catch (err) {
        console.warn("Firebase Auth Init Warning:", err);
      }

      // UI Alert Manager
      function showBannerAlert(message, type = 'error') {
        const alertBox = document.getElementById('dynamic-alert');
        const alertMsg = document.getElementById('dynamic-alert-msg');
        const alertIcon = document.getElementById('dynamic-alert-icon');
        if (alertBox && alertMsg) {
          alertMsg.innerText = message;
          alertBox.classList.remove('hidden', 'bg-rose-50', 'border-rose-500', 'text-rose-800', 
                                   'bg-emerald-50', 'border-emerald-500', 'text-emerald-800', 
                                   'bg-blue-50', 'border-blue-500', 'text-blue-800');
          if (alertIcon) {
            alertIcon.classList.remove('text-rose-500', 'text-emerald-500', 'text-blue-500');
          }

          if (type === 'success') {
            alertBox.classList.add('bg-emerald-50', 'border-emerald-500', 'text-emerald-800');
            if (alertIcon) alertIcon.classList.add('text-emerald-500');
          } else if (type === 'info') {
            alertBox.classList.add('bg-blue-50', 'border-blue-500', 'text-blue-800');
            if (alertIcon) alertIcon.classList.add('text-blue-500');
          } else {
            alertBox.classList.add('bg-rose-50', 'border-rose-500', 'text-rose-800');
            if (alertIcon) alertIcon.classList.add('text-rose-500');
          }
        }
      }

      function togglePasswordVisibility(inputId, eyeOnId, eyeOffId) {
        const passwordInput = document.getElementById(inputId);
        const eyeOn = document.getElementById(eyeOnId);
        const eyeOff = document.getElementById(eyeOffId);
        
        if (passwordInput.type === 'password') {
          passwordInput.type = 'text';
          eyeOn.classList.remove('hidden');
          eyeOff.classList.add('hidden');
        } else {
          passwordInput.type = 'password';
          eyeOn.classList.add('hidden');
          eyeOff.classList.remove('hidden');
        }
      }

      let currentView = '<%= view %>';
      let isRegisterMode = false;

      function switchView(targetView, extraData = {}) {
        currentView = targetView;
        const authContainer = document.getElementById('auth-view-container');
        const forgotContainer = document.getElementById('forgot-view-container');
        const verifyContainer = document.getElementById('verify-view-container');
        const setpwContainer = document.getElementById('setpw-view-container');
        const viewBadge = document.getElementById('view-badge');

        [authContainer, forgotContainer, verifyContainer, setpwContainer].forEach(c => {
          if (c) c.classList.add('hidden');
        });

        if (targetView === 'forgot') {
          if (forgotContainer) forgotContainer.classList.remove('hidden');
          if (viewBadge) viewBadge.innerText = 'Reset Password';
          const emailVal = document.getElementById('email').value;
          if (emailVal) {
            document.getElementById('forgot-email').value = emailVal;
          }
        } else if (targetView === 'verify') {
          if (verifyContainer) verifyContainer.classList.remove('hidden');
          if (viewBadge) viewBadge.innerText = 'Verification';
          if (extraData.email) {
            const emailSpan = document.getElementById('verify-email-display');
            if (emailSpan) emailSpan.innerText = extraData.email;
          }
        } else if (targetView === 'setpw') {
          if (setpwContainer) setpwContainer.classList.remove('hidden');
          if (viewBadge) viewBadge.innerText = 'Set Password';
        } else {
          if (authContainer) authContainer.classList.remove('hidden');
          if (viewBadge) viewBadge.innerText = isRegisterMode ? 'Register' : 'Sign In';
        }
      }

      function toggleAuthMode() {
        isRegisterMode = !isRegisterMode;
        const heading = document.getElementById('form-heading');
        const subtitle = document.getElementById('form-subtitle');
        const submitBtnText = document.getElementById('submit-btn-text');
        const promptText = document.getElementById('toggle-prompt-text');
        const toggleBtn = document.getElementById('toggle-auth-btn');
        const isRegisterInput = document.getElementById('isRegister');
        const viewBadge = document.getElementById('view-badge');
        const registerNotice = document.getElementById('register-info-notice');
        const loginOptionsRow = document.getElementById('login-options-row');

        if (isRegisterMode) {
          if (heading) heading.innerText = "Create Account";
          if (subtitle) subtitle.innerText = "Enter your email and password to register a new account.";
          if (submitBtnText) submitBtnText.innerText = "Register";
          if (promptText) promptText.innerText = "Already Have An Account? ";
          if (toggleBtn) toggleBtn.innerText = "Log In.";
          if (isRegisterInput) isRegisterInput.value = "true";
          if (viewBadge) viewBadge.innerText = "Register";
          if (registerNotice) registerNotice.classList.remove('hidden');
          if (loginOptionsRow) loginOptionsRow.classList.add('hidden');
        } else {
          if (heading) heading.innerText = "Welcome Back";
          if (subtitle) subtitle.innerText = "Enter your email and password to access your account.";
          if (submitBtnText) submitBtnText.innerText = "Log In";
          if (promptText) promptText.innerText = "Don't Have An Account? ";
          if (toggleBtn) toggleBtn.innerText = "Register Now.";
          if (isRegisterInput) isRegisterInput.value = "false";
          if (viewBadge) viewBadge.innerText = "Sign In";
          if (registerNotice) registerNotice.classList.add('hidden');
          if (loginOptionsRow) loginOptionsRow.classList.remove('hidden');
        }
      }

      // Handle Forgot Password Form Submission
      function handleForgotPassword(e) {
        e.preventDefault();
        const emailInput = document.getElementById('forgot-email');
        const email = emailInput ? emailInput.value.trim() : "";
        const submitBtn = document.getElementById('forgot-submit-btn');
        const spinner = document.getElementById('forgot-spinner');
        const btnText = document.getElementById('forgot-btn-text');

        if (!email) {
          showBannerAlert("Please enter your email address.", "error");
          return;
        }

        if (submitBtn) submitBtn.disabled = true;
        if (spinner) spinner.classList.remove('hidden');
        if (btnText) btnText.innerText = "Sending Link...";

        // Try Firebase SDK first
        firebase.auth().sendPasswordResetEmail(email)
          .then(() => {
            showBannerAlert("Password reset link sent! Please check your inbox and spam folder.", "success");
            setTimeout(() => {
              switchView('login');
              showBannerAlert("Password reset link sent to " + email + ". Check your inbox.", "success");
            }, 1800);
          })
          .catch((error) => {
            console.warn("Client Firebase password reset error:", error);
            // Fallback: Submit directly to backend
            const formData = new URLSearchParams();
            formData.append('action', 'forgotPassword');
            formData.append('email', email);
            formData.append('isAjax', 'true');

            fetch('/login', {
              method: 'POST',
              headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
              body: formData.toString()
            })
            .then(res => res.json())
            .then(data => {
              showBannerAlert("Password reset link sent! Check your inbox.", "success");
              setTimeout(() => {
                switchView('login');
                showBannerAlert("Password reset link sent to " + email + ".", "success");
              }, 1800);
            })
            .catch(() => {
              document.getElementById('forgotForm').submit();
            });
          })
          .finally(() => {
            if (submitBtn) submitBtn.disabled = false;
            if (spinner) spinner.classList.add('hidden');
            if (btnText) btnText.innerText = "Send Reset Link";
          });
      }

      // Resend Email Verification
      function resendEmailVerification() {
        const btn = document.getElementById('resend-verification-btn');
        if (btn) {
          btn.disabled = true;
          btn.innerText = "Dispatching...";
        }

        const user = firebase.auth().currentUser;
        if (user) {
          user.sendEmailVerification()
            .then(() => {
              showBannerAlert("Verification email re-sent successfully!", "success");
              startResendCountdown(btn);
            })
            .catch((err) => {
              showBannerAlert(err.message || "Failed to resend verification email.", "error");
              if (btn) {
                btn.disabled = false;
                btn.innerText = "Resend Verification Email";
              }
            });
        } else {
          // Send backend verification request
          const emailVal = document.getElementById('email').value || "<%= emailParam %>";
          const formData = new URLSearchParams();
          formData.append('action', 'resendVerification');
          formData.append('email', emailVal);
          formData.append('isAjax', 'true');

          fetch('/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: formData.toString()
          })
          .then(() => {
            showBannerAlert("Verification email re-sent! Check your inbox.", "success");
            startResendCountdown(btn);
          })
          .catch(() => {
            showBannerAlert("Failed to resend verification email.", "error");
            if (btn) {
              btn.disabled = false;
              btn.innerText = "Resend Verification Email";
            }
          });
        }
      }

      function startResendCountdown(btn) {
        if (!btn) return;
        let countdown = 30;
        btn.disabled = true;
        const interval = setInterval(() => {
          btn.innerText = "Resend again in " + countdown + "s";
          countdown--;
          if (countdown < 0) {
            clearInterval(interval);
            btn.disabled = false;
            btn.innerText = "Resend Verification Email";
          }
        }, 1000);
      }

      // Firebase Web SDK Google Sign-In
      function loginWithGoogle() {
        showBannerAlert("Connecting with Google...", "info");
        try {
          const provider = new firebase.auth.GoogleAuthProvider();
          provider.addScope('email');
          provider.addScope('profile');
          provider.setCustomParameters({ prompt: 'select_account' });

          firebase.auth().signInWithPopup(provider)
            .then((result) => {
              showBannerAlert("Google authenticated! Logging in...", "success");
              return result.user.getIdToken().then((idToken) => {
                document.getElementById('firebaseToken').value = idToken;
                if (result.user.email) {
                  document.getElementById('email').value = result.user.email;
                }
                document.getElementById('loginForm').submit();
              });
            })
            .catch((error) => {
              console.error("Firebase Google Sign-In Error:", error);
              if (error.code === 'auth/popup-closed-by-user') {
                showBannerAlert("Sign-in popup was closed.", "info");
              } else if (error.code === 'auth/unauthorized-domain' || (error.message && error.message.includes('redirect_uri_mismatch'))) {
                showBannerAlert("OAuth Error: Register 'https://" + (defaultFirebaseConfig.authDomain || "cresuit-app.firebaseapp.com") + "/__/auth/handler' in Google Cloud Console.", "error");
              } else {
                showBannerAlert(error.message || "Google Sign-In failed.", "error");
              }
            });
        } catch (e) {
          console.error("Firebase Auth Exception:", e);
          showBannerAlert("Firebase Authentication error: " + e.message, "error");
        }
      }

      // Check redirect result on page load if using redirect flow
      try {
        firebase.auth().getRedirectResult().then((result) => {
          if (result && result.user) {
            showBannerAlert("Google authenticated! Logging in...", "success");
            result.user.getIdToken().then((idToken) => {
              document.getElementById('firebaseToken').value = idToken;
              if (result.user.email) {
                document.getElementById('email').value = result.user.email;
              }
              document.getElementById('loginForm').submit();
            });
          }
        }).catch((err) => {
          if (err && err.message) {
            console.error("Firebase redirect result error:", err);
          }
        });
      } catch (err) {}

      // Social Auth (Apple, etc.)
      function loginWithFirebaseAuth(providerType) {
        if (providerType === 'google') {
          loginWithGoogle();
          return;
        }

        const loginForm = document.getElementById('loginForm');
        let provider;

        if (providerType === 'apple') {
          provider = new firebase.auth.OAuthProvider('apple.com');
        }

        if (provider) {
          firebase.auth().signInWithPopup(provider)
            .then((result) => {
              return result.user.getIdToken().then((idToken) => {
                document.getElementById('firebaseToken').value = idToken;
                if (result.user.email) {
                  document.getElementById('email').value = result.user.email;
                }
                loginForm.submit();
              });
            })
            .catch((error) => {
              showBannerAlert(error.message || "Social login failed.", "error");
            });
        }
      }

      // Main Form Submit Handler
      document.getElementById('loginForm').addEventListener('submit', function(e) {
        const firebaseTokenInput = document.getElementById('firebaseToken');
        const email = document.getElementById('email').value.trim();
        const password = document.getElementById('password').value;
        const isRegister = document.getElementById('isRegister').value === "true";
        const submitBtn = document.getElementById('submit-btn');
        const spinner = document.getElementById('submit-spinner');
        const submitBtnText = document.getElementById('submit-btn-text');

        if (!firebaseTokenInput.value && email && password) {
          e.preventDefault();
          if (submitBtn) submitBtn.disabled = true;
          if (spinner) spinner.classList.remove('hidden');
          if (submitBtnText) submitBtnText.innerText = isRegister ? "Registering..." : "Logging In...";

          const authAction = isRegister
            ? firebase.auth().createUserWithEmailAndPassword(email, password)
            : firebase.auth().signInWithEmailAndPassword(email, password);

          authAction
            .then((userCredential) => {
              // If registration, dispatch email verification
              if (isRegister && userCredential.user) {
                userCredential.user.sendEmailVerification().catch((e) => console.warn("Email verification send warning:", e));
              }
              return userCredential.user.getIdToken();
            })
            .then((idToken) => {
              firebaseTokenInput.value = idToken;
              document.getElementById('loginForm').submit();
            })
            .catch((err) => {
              console.warn("Client Firebase Auth failed, falling back to server:", err);
              // If Firebase Auth reported user-not-found on login, prompt user
              if (err.code === 'auth/user-not-found') {
                showBannerAlert("No account found with this email. Click 'Register Now' below to create one.", "error");
                if (submitBtn) submitBtn.disabled = false;
                if (spinner) spinner.classList.add('hidden');
                if (submitBtnText) submitBtnText.innerText = "Log In";
                return;
              }
              if (err.code === 'auth/wrong-password') {
                showBannerAlert("Incorrect password. Please try again or click 'Forgot Password?'.", "error");
                if (submitBtn) submitBtn.disabled = false;
                if (spinner) spinner.classList.add('hidden');
                if (submitBtnText) submitBtnText.innerText = "Log In";
                return;
              }
              if (err.code === 'auth/email-already-in-use') {
                showBannerAlert("This email is already registered. Please log in or reset your password.", "error");
                if (submitBtn) submitBtn.disabled = false;
                if (spinner) spinner.classList.add('hidden');
                if (submitBtnText) submitBtnText.innerText = "Register";
                return;
              }
              // Submit to backend fallback
              document.getElementById('loginForm').submit();
            });
        }
      });
    </script>
  </body>
</html>