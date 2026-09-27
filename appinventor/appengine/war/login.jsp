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
<html lang="en" class="h-full">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <meta http-equiv="pragma" content="no-cache"/>
    <meta http-equiv="Cache-Control" content="no-cache, must-revalidate"/>
    <meta http-equiv="expires" content="0"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>CreSuit - Sign In</title>
    <!-- Favicon -->
    <link rel="icon" type="image/png" href="https://aibaljose.github.io/CreSuit/img/play_store_512.png">
    <link rel="shortcut icon" type="image/png" href="https://aibaljose.github.io/CreSuit/img/play_store_512.png">
    
    <!-- Tailwind CSS v3 CDN -->
    <script src="https://cdn.tailwindcss.com"></script>
    
    <!-- Google Fonts Inter -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    
    <!-- Firebase Web SDK (Compat mode) -->
    <script src="https://www.gstatic.com/firebasejs/10.8.0/firebase-app-compat.js"></script>
    <script src="https://www.gstatic.com/firebasejs/10.8.0/firebase-auth-compat.js"></script>

    <style>
      body {
        font-family: 'Inter', -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
        font-feature-settings: 'cv02', 'cv03', 'cv04', 'cv11';
        -webkit-font-smoothing: antialiased;
        -moz-osx-font-smoothing: grayscale;
      }
      .fade-in {
        animation: fadeIn 0.2s cubic-bezier(0.16, 1, 0.3, 1) forwards;
      }
      @keyframes fadeIn {
        from { opacity: 0; transform: translateY(4px); }
        to { opacity: 1; transform: translateY(0); }
      }
      .dev-grid {
        background-image: 
          radial-gradient(rgba(255, 255, 255, 0.08) 1px, transparent 1px);
        background-size: 20px 20px;
      }
      .builder-scroll::-webkit-scrollbar {
        width: 4px;
        height: 4px;
      }
      .builder-scroll::-webkit-scrollbar-thumb {
        background: rgba(255, 255, 255, 0.15);
        border-radius: 4px;
      }
    </style>
  </head>
  <body class="min-h-screen w-full bg-white antialiased text-slate-800 flex flex-col lg:flex-row overflow-x-hidden">
    
    <!-- ================= LEFT COLUMN: AUTHENTICATION FORM ================= -->
    <div class="w-full lg:w-[48%] xl:w-[46%] min-h-screen flex flex-col justify-between p-6 sm:p-10 lg:p-12 xl:p-16 bg-white relative z-10 border-r border-slate-100">
      
      <!-- Top Bar: Brand, Home navigation & View Badge -->
      <header class="flex items-center justify-between w-full max-w-[460px] mx-auto">
        <a href="/landing.jsp" class="flex items-center space-x-2.5 text-slate-900 group transition">
          <div class="w-8 h-8 rounded-lg bg-blue-600 flex items-center justify-center text-white shadow-xs group-hover:bg-blue-700 transition">
            <!-- CreSuit Technical Symbol -->
            <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="3" width="7" height="7" rx="1.5" />
              <rect x="14" y="3" width="7" height="7" rx="1.5" />
              <rect x="14" y="14" width="7" height="7" rx="1.5" />
              <rect x="3" y="14" width="7" height="7" rx="1.5" />
            </svg>
          </div>
          <span class="text-lg font-semibold tracking-tight text-slate-900">CreSuit</span>
        </a>

        <div class="flex items-center space-x-2.5">
          <a href="/landing.jsp" class="text-xs font-medium text-slate-500 hover:text-slate-800 transition px-2.5 py-1.5 rounded-md hover:bg-slate-50 inline-flex items-center space-x-1">
            <span>Home</span>
          </a>
          <!-- View State Badge -->
          <span id="view-badge" class="text-[11px] font-medium text-slate-600 bg-slate-100 border border-slate-200/80 px-2.5 py-1 rounded-md">
            Sign In
          </span>
        </div>
      </header>

      <!-- Main Form Container -->
      <main class="w-full max-w-[460px] mx-auto my-auto py-8 sm:py-10">
            
        <!-- Dynamic Alert Banner -->
        <div id="dynamic-alert" class="hidden mb-5 p-3.5 rounded-lg border shadow-xs transition duration-200 text-xs">
          <div class="flex items-start space-x-2.5">
            <svg id="dynamic-alert-icon" class="h-4 w-4 shrink-0 mt-0.5" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clip-rule="evenodd" />
            </svg>
            <p id="dynamic-alert-msg" class="font-medium leading-relaxed"></p>
          </div>
        </div>

        <% if (error != null && !error.isEmpty()) { %>
        <div class="mb-5 bg-rose-50 border border-rose-200 p-3.5 rounded-lg shadow-xs text-xs">
          <div class="flex items-start space-x-2.5">
            <svg class="h-4 w-4 text-rose-600 shrink-0 mt-0.5" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clip-rule="evenodd" />
            </svg>
            <p class="font-medium text-rose-800 leading-relaxed"><%= error %></p>
          </div>
        </div>
        <% } %>

        <% if ("linksent".equals(status)) { %>
        <div class="mb-5 bg-emerald-50 border border-emerald-200 p-3.5 rounded-lg shadow-xs text-xs">
          <div class="flex items-start space-x-2.5">
            <svg class="h-4 w-4 text-emerald-600 shrink-0 mt-0.5" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
            </svg>
            <p class="font-medium text-emerald-800 leading-relaxed">Password reset link sent. Please check your inbox.</p>
          </div>
        </div>
        <% } else if ("verification_sent".equals(status)) { %>
        <div class="mb-5 bg-blue-50 border border-blue-200 p-3.5 rounded-lg shadow-xs text-xs">
          <div class="flex items-start space-x-2.5">
            <svg class="h-4 w-4 text-blue-600 shrink-0 mt-0.5" viewBox="0 0 20 20" fill="currentColor">
              <path d="M2.003 5.884L10 9.882l7.997-3.998A2 2 0 0016 4H4a2 2 0 00-1.997 1.884z" />
              <path d="M18 8.118l-8 4-8-4V14a2 2 0 002 2h12a2 2 0 002-2V8.118z" />
            </svg>
            <p class="font-medium text-blue-800 leading-relaxed">Email verification link sent. Please verify your email before continuing.</p>
          </div>
        </div>
        <% } else if ("password_updated".equals(status)) { %>
        <div class="mb-5 bg-emerald-50 border border-emerald-200 p-3.5 rounded-lg shadow-xs text-xs">
          <div class="flex items-start space-x-2.5">
            <svg class="h-4 w-4 text-emerald-600 shrink-0 mt-0.5" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
            </svg>
            <p class="font-medium text-emerald-800 leading-relaxed">Password updated successfully. You can now sign in with your new password.</p>
          </div>
        </div>
        <% } %>

        <!-- ================= VIEW 1 & 2: LOGIN & REGISTER ================= -->
        <div id="auth-view-container" class="fade-in <%= ("forgot".equals(view) || "verify".equals(view) || "setpw".equals(view)) ? "hidden" : "" %>">
          
          <!-- Header Area -->
          <div class="mb-6">
            <h1 id="form-heading" class="text-2xl sm:text-3xl font-semibold text-slate-900 tracking-tight">Welcome back</h1>
            <p id="form-subtitle" class="text-sm text-slate-500 mt-2 font-normal">Sign in to continue building with CreSuit.</p>
          </div>

          <!-- Credentials Form -->
          <form id="loginForm" method="POST" action="/login" class="space-y-4">
            
            <!-- Email Field -->
            <div>
              <label for="email" class="block text-xs font-medium text-slate-700 mb-1.5">
                Email address
              </label>
              <input id="email" name="email" type="email" autocomplete="email" required 
                     value="<%= emailParam %>"
                     placeholder="name@company.com"
                     class="w-full h-12 px-3.5 rounded-lg border border-slate-300 bg-white text-sm text-slate-900 placeholder:text-slate-400 hover:border-slate-400 focus:outline-none focus:border-blue-600 focus:ring-4 focus:ring-blue-500/10 transition-colors shadow-xs">
            </div>

            <!-- Password Field -->
            <div>
              <div class="flex items-center justify-between mb-1.5">
                <label for="password" class="block text-xs font-medium text-slate-700">
                  Password
                </label>
              </div>
              <div class="relative">
                <input id="password" name="password" type="password" autocomplete="current-password" required 
                       placeholder="••••••••"
                       class="w-full h-12 px-3.5 pr-11 rounded-lg border border-slate-300 bg-white text-sm text-slate-900 placeholder:text-slate-400 hover:border-slate-400 focus:outline-none focus:border-blue-600 focus:ring-4 focus:ring-blue-500/10 transition-colors shadow-xs">
                
                <button type="button" 
                        onclick="togglePasswordVisibility('password', 'eye-icon-on', 'eye-icon-off')" 
                        aria-label="Toggle password visibility"
                        class="absolute right-2.5 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 focus:outline-none p-1.5 rounded-md hover:bg-slate-100 transition-colors">
                  <svg id="eye-icon-off" class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858-5.908a10.04 10.04 0 013.122-.463c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m-4.692-4.692a3 3 0 00-4.243-4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l18 18" />
                  </svg>
                  <svg id="eye-icon-on" class="w-4 h-4 hidden" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                    <path stroke-linecap="round" stroke-linejoin="round" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                  </svg>
                </button>
              </div>
            </div>

            <!-- Remember Me & Forgot Password Row -->
            <div id="login-options-row" class="flex items-center justify-between pt-1">
              <label class="flex items-center space-x-2 cursor-pointer select-none">
                <input type="checkbox" id="remember" class="w-4 h-4 rounded border-slate-300 text-blue-600 focus:ring-blue-500 focus:ring-offset-0 cursor-pointer">
                <span class="text-xs font-normal text-slate-600">Remember me</span>
              </label>
              
              <button type="button" onclick="switchView('forgot')" 
                 class="text-xs font-medium text-blue-600 hover:text-blue-700 hover:underline transition-colors cursor-pointer">
                Forgot password?
              </button>
            </div>

            <!-- Register Info Notice -->
            <div id="register-info-notice" class="hidden text-xs text-slate-600 bg-slate-50 border border-slate-200 p-3 rounded-lg">
              <div class="flex items-center space-x-1.5 text-blue-700 font-medium mb-1">
                <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                <span>Email verification</span>
              </div>
              <p class="leading-relaxed text-slate-500">A verification link will be sent to confirm your email address and activate your account.</p>
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

            <!-- Primary Submit Button -->
            <div class="pt-2">
              <button type="submit" id="submit-btn" 
                      class="w-full h-12 bg-blue-600 hover:bg-blue-700 active:bg-blue-800 text-white font-medium text-sm rounded-lg shadow-sm transition-colors duration-150 cursor-pointer flex items-center justify-center space-x-2 focus:outline-none focus:ring-4 focus:ring-blue-500/20">
                <span id="submit-btn-text">Sign In</span>
                <svg id="submit-spinner" class="animate-spin h-4 w-4 text-white hidden" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
              </button>
            </div>
          </form>

          <!-- Divider: "or continue with" -->
          <div id="social-divider" class="relative flex items-center justify-center my-6">
            <div class="absolute inset-0 flex items-center" aria-hidden="true">
              <div class="w-full border-t border-slate-200"></div>
            </div>
            <div class="relative bg-white px-3 text-xs font-normal text-slate-400">
              or continue with
            </div>
          </div>

          <!-- OAuth Social Authentication Buttons -->
          <div id="social-buttons-grid" class="grid grid-cols-2 gap-3">
            <!-- Google OAuth Button -->
            <button type="button" onclick="loginWithGoogle()" class="h-11 flex items-center justify-center gap-2 px-3 rounded-lg border border-slate-200 bg-white hover:bg-slate-50 hover:border-slate-300 text-xs font-medium text-slate-700 shadow-xs transition-colors cursor-pointer focus:outline-none focus:ring-2 focus:ring-slate-400/20">
              <svg class="h-4 w-4 shrink-0" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4"/>
                <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/>
                <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z" fill="#FBBC05"/>
                <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z" fill="#EA4335"/>
              </svg>
              <span>Google</span>
            </button>

            <!-- Apple OAuth Button -->
            <button type="button" onclick="loginWithFirebaseAuth('apple')" class="h-11 flex items-center justify-center gap-2 px-3 rounded-lg border border-slate-200 bg-white hover:bg-slate-50 hover:border-slate-300 text-xs font-medium text-slate-700 shadow-xs transition-colors cursor-pointer focus:outline-none focus:ring-2 focus:ring-slate-400/20">
              <svg class="h-4 w-4 text-slate-900 fill-current shrink-0" viewBox="0 0 24 24">
                <path d="M18.71 19.5c-.83 1.24-1.71 2.45-3.05 2.47-1.34.03-1.77-.79-3.29-.79-1.53 0-2 .77-3.27.82-1.31.05-2.3-1.32-3.14-2.53C4.25 17 2.94 12.45 4.7 9.39c.87-1.52 2.43-2.48 4.12-2.51 1.28-.02 2.5.87 3.29.87.78 0 2.26-1.07 3.81-.91.65.03 2.47.26 3.64 1.98-.09.06-2.17 1.28-2.15 3.81.03 3.02 2.65 4.03 2.68 4.04-.03.07-.42 1.44-1.38 2.83M15.97 6.32c.67-.82 1.13-1.97.99-3.12-1 .04-2.22.67-2.92 1.49-.63.73-1.18 1.91-1.03 3.03 1.12.09 2.28-.58 2.96-1.4z"/>
              </svg>
              <span>Apple</span>
            </button>
          </div>

          <!-- Registration / Sign In Toggle Switch -->
          <div class="mt-6 text-center">
            <p class="text-xs text-slate-500">
              <span id="toggle-prompt-text">Don't have an account? </span>
              <a href="javascript:void(0)" id="toggle-auth-btn" onclick="toggleAuthMode()" class="text-blue-600 font-medium hover:text-blue-700 hover:underline transition-colors ml-0.5">Create one</a>
            </p>
          </div>

        </div>

        <!-- ================= VIEW 3: FORGOT PASSWORD ================= -->
        <div id="forgot-view-container" class="fade-in <%= "forgot".equals(view) ? "" : "hidden" %>">
          
          <div class="mb-6">
            <button type="button" class="inline-flex items-center space-x-1.5 text-xs font-medium text-slate-500 hover:text-slate-800 mb-3 cursor-pointer transition" onclick="switchView('login')">
              <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
              </svg>
              <span>Back to sign in</span>
            </button>
            <h1 class="text-2xl sm:text-3xl font-semibold text-slate-900 tracking-tight">Reset password</h1>
            <p class="text-sm text-slate-500 mt-2 font-normal">Enter your email address and we will send you a password reset link.</p>
          </div>

          <form id="forgotForm" method="POST" action="/login" class="space-y-4" onsubmit="handleForgotPassword(event)">
            <input type="hidden" name="action" value="forgotPassword">
            <input type="hidden" name="locale" value="<%= locale %>">
            
            <div>
              <label for="forgot-email" class="block text-xs font-medium text-slate-700 mb-1.5">
                Email address
              </label>
              <input id="forgot-email" name="email" type="email" autocomplete="email" required 
                     value="<%= emailParam %>"
                     placeholder="name@company.com"
                     class="w-full h-12 px-3.5 rounded-lg border border-slate-300 bg-white text-sm text-slate-900 placeholder:text-slate-400 hover:border-slate-400 focus:outline-none focus:border-blue-600 focus:ring-4 focus:ring-blue-500/10 transition-colors shadow-xs">
            </div>

            <div class="pt-2">
              <button type="submit" id="forgot-submit-btn" 
                      class="w-full h-12 bg-blue-600 hover:bg-blue-700 active:bg-blue-800 text-white font-medium text-sm rounded-lg shadow-sm transition-colors duration-150 cursor-pointer flex items-center justify-center space-x-2 focus:outline-none focus:ring-4 focus:ring-blue-500/20">
                <span id="forgot-btn-text">Send reset link</span>
                <svg id="forgot-spinner" class="animate-spin h-4 w-4 text-white hidden" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
              </button>
            </div>
          </form>

          <div class="mt-6 p-3.5 rounded-lg bg-slate-50 border border-slate-200 text-xs text-slate-500 leading-relaxed">
            <p>Make sure to enter the exact email associated with your CreSuit workspace.</p>
          </div>

        </div>

        <!-- ================= VIEW 4: EMAIL VERIFICATION SENT ================= -->
        <div id="verify-view-container" class="fade-in <%= "verify".equals(view) ? "" : "hidden" %>">
          
          <div class="text-left py-2">
            <div class="w-10 h-10 bg-blue-50 text-blue-600 rounded-lg flex items-center justify-center mb-4 border border-blue-100">
              <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
              </svg>
            </div>
            
            <h2 class="text-2xl font-semibold text-slate-900 tracking-tight">Verify your email</h2>
            <p class="text-sm text-slate-500 mt-2 leading-relaxed">
              We sent a verification link to <span id="verify-email-display" class="font-medium text-slate-800"><%= emailParam.isEmpty() ? "your email address" : emailParam %></span>.
            </p>
            
            <p class="text-xs text-slate-400 mt-2 leading-relaxed">
              Please click the link in the message to activate your account. If you don't see it within a minute, check your spam or junk folder.
            </p>

            <div class="mt-6 space-y-2.5">
              <button type="button" id="resend-verification-btn" onclick="resendEmailVerification()"
                      class="w-full h-11 bg-white border border-slate-300 hover:bg-slate-50 text-slate-700 font-medium rounded-lg text-xs transition duration-150 cursor-pointer shadow-xs">
                Resend verification email
              </button>

              <button type="button" onclick="switchView('login')" 
                      class="w-full h-11 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg text-xs transition duration-150 cursor-pointer">
                Back to sign in
              </button>
            </div>
          </div>

        </div>

        <!-- ================= VIEW 5: SET NEW PASSWORD ================= -->
        <div id="setpw-view-container" class="fade-in <%= "setpw".equals(view) ? "" : "hidden" %>">
          
          <div class="mb-6">
            <h1 class="text-2xl sm:text-3xl font-semibold text-slate-900 tracking-tight">Set new password</h1>
            <p class="text-sm text-slate-500 mt-2 font-normal">
              <% if (resetEmail != null && !resetEmail.isEmpty()) { %>
                Setting password for <span class="font-medium text-slate-800"><%= resetEmail %></span>
              <% } else { %>
                Enter a strong password for your account.
              <% } %>
            </p>
          </div>

          <form id="setPwForm" method="POST" action="/login/<%= resetUid != null ? resetUid : "" %>/setpw" class="space-y-4">
            <input type="hidden" name="uid" value="<%= resetUid != null ? resetUid : "" %>">
            <input type="hidden" name="locale" value="<%= locale %>">
            
            <div>
              <label for="new-password" class="block text-xs font-medium text-slate-700 mb-1.5">
                New password
              </label>
              <div class="relative">
                <input id="new-password" name="password" type="password" required 
                       placeholder="••••••••"
                       class="w-full h-12 px-3.5 pr-11 rounded-lg border border-slate-300 bg-white text-sm text-slate-900 placeholder:text-slate-400 hover:border-slate-400 focus:outline-none focus:border-blue-600 focus:ring-4 focus:ring-blue-500/10 transition-colors shadow-xs">
                
                <button type="button" onclick="togglePasswordVisibility('new-password', 'setpw-eye-on', 'setpw-eye-off')" 
                        aria-label="Toggle password visibility"
                        class="absolute right-2.5 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 focus:outline-none p-1.5 rounded-md hover:bg-slate-100 transition-colors">
                  <svg id="setpw-eye-off" class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858-5.908a10.04 10.04 0 013.122-.463c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m-4.692-4.692a3 3 0 00-4.243-4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l18 18" />
                  </svg>
                  <svg id="setpw-eye-on" class="w-4 h-4 hidden" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                    <path stroke-linecap="round" stroke-linejoin="round" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                  </svg>
                </button>
              </div>
            </div>

            <div class="pt-2">
              <button type="submit" 
                      class="w-full h-12 bg-blue-600 hover:bg-blue-700 active:bg-blue-800 text-white font-medium text-sm rounded-lg shadow-sm transition-colors duration-150 cursor-pointer">
                Update password & sign in
              </button>
            </div>
          </form>

        </div>

      </main>

      <!-- Left Column Footer -->
      <footer class="pt-4 border-t border-slate-100 flex items-center justify-between text-xs text-slate-400 font-normal w-full max-w-[460px] mx-auto">
        <span>&copy; 2026 CreSuit</span>
        
        <!-- Language selector links -->
        <div class="inline-flex items-center space-x-2 text-xs">
          <a href="<%= new UriBuilder("/login").add("locale", "zh_CN").add("repo", repo).add("autoload", autoload).add("galleryId", galleryId).add("ui", uiPreference).add("redirect", redirect).build() %>" 
             class="<%= locale.equals("zh_CN") ? "font-semibold text-slate-700" : "hover:text-slate-600" %>">ZH</a>
          <span class="text-slate-300">·</span>
          <a href="<%= new UriBuilder("/login").add("locale", "pt").add("repo", repo).add("autoload", autoload).add("galleryId", galleryId).add("ui", uiPreference).add("redirect", redirect).build() %>" 
             class="<%= locale.equals("pt") ? "font-semibold text-slate-700" : "hover:text-slate-600" %>">PT</a>
          <span class="text-slate-300">·</span>
          <a href="<%= new UriBuilder("/login").add("locale", "en").add("repo", repo).add("autoload", autoload).add("galleryId", galleryId).add("ng", newGalleryId).add("ui", uiPreference).add("redirect", redirect).build() %>" 
             class="<%= locale.equals("en") ? "font-semibold text-slate-700" : "hover:text-slate-600" %>">EN</a>
        </div>
      </footer>

    </div>

    <!-- ================= RIGHT COLUMN: PRODUCT SHOWCASE ================= -->
    <div class="hidden lg:flex lg:w-[52%] xl:w-[54%] min-h-screen bg-[#0A0F1D] p-8 lg:p-12 xl:p-14 text-white flex-col justify-between relative overflow-hidden select-none">
      
      <!-- Subtle Developer Grid Pattern & Subtle Depth Highlights -->
      <div class="absolute inset-0 dev-grid opacity-35 pointer-events-none"></div>
      <div class="absolute -top-32 -right-32 w-96 h-96 bg-blue-600/10 rounded-full blur-3xl pointer-events-none"></div>
      <div class="absolute -bottom-32 -left-32 w-96 h-96 bg-indigo-600/10 rounded-full blur-3xl pointer-events-none"></div>
      
      <!-- Hero Product Messaging -->
      <div class="relative z-10 max-w-xl">
        <div class="inline-flex items-center space-x-2 px-2.5 py-1 rounded-md bg-blue-500/10 border border-blue-500/20 text-[11px] font-medium text-blue-400 mb-3.5">
          <span class="w-1.5 h-1.5 rounded-full bg-blue-400 animate-pulse"></span>
          <span>AI-Assisted App Builder</span>
        </div>
        <h2 class="text-2xl xl:text-3xl font-semibold tracking-tight text-white leading-snug">
          Build apps without starting from scratch.
        </h2>
        <p class="text-slate-400 text-xs xl:text-sm font-normal mt-2 leading-relaxed max-w-md">
          Design, prototype and build mobile apps visually with AI assistance.
        </p>
      </div>

      <!-- Realistic CreSuit Application Builder Interface Mockup -->
      <div class="relative z-10 my-4 w-full max-w-2xl mx-auto">
        <div class="bg-[#10172A] rounded-xl border border-slate-700/60 shadow-2xl overflow-hidden flex flex-col text-slate-300">
          
          <!-- Mockup Top Window Header Bar -->
          <div class="h-9 px-3.5 bg-[#0D1322] border-b border-slate-700/60 flex items-center justify-between">
            <div class="flex items-center space-x-2">
              <div class="flex items-center space-x-1.5">
                <span class="w-2.5 h-2.5 rounded-full bg-rose-500/70"></span>
                <span class="w-2.5 h-2.5 rounded-full bg-amber-500/70"></span>
                <span class="w-2.5 h-2.5 rounded-full bg-emerald-500/70"></span>
              </div>
              <span class="text-slate-600 text-xs font-mono ml-2">|</span>
              <span class="text-[11px] font-medium text-slate-300">OrderTracker · Screen1</span>
            </div>

            <!-- Builder Mode Switches & AI Status Pill -->
            <div class="flex items-center space-x-2">
              <div class="flex items-center bg-[#151E33] border border-slate-700/80 rounded px-1.5 py-0.5 text-[10px] space-x-1">
                <span class="bg-blue-600 text-white font-medium px-1.5 py-0.5 rounded shadow-xs">Designer</span>
                <span class="text-slate-400 hover:text-slate-200 px-1.5 py-0.5">Blocks</span>
              </div>
              <div class="inline-flex items-center space-x-1 px-2 py-0.5 rounded bg-emerald-500/10 border border-emerald-500/20 text-[10px] text-emerald-400 font-medium">
                <span class="w-1 h-1 rounded-full bg-emerald-400"></span>
                <span>AI Ready</span>
              </div>
            </div>
          </div>

          <!-- Mockup 3-Column Studio Workspace -->
          <div class="grid grid-cols-12 h-[330px] text-xs">
            
            <!-- 1. Left Component & Tree Panel (Col 3) -->
            <div class="col-span-3 border-r border-slate-700/60 bg-[#0E1526] p-2.5 flex flex-col justify-between">
              <div>
                <div class="text-[9px] font-semibold text-slate-400 uppercase tracking-wider mb-2">Palette</div>
                <div class="space-y-1 text-[10px]">
                  <div class="flex items-center space-x-1.5 px-2 py-1.5 rounded bg-[#162038] text-slate-200 border border-slate-700/50">
                    <svg class="w-3 h-3 text-blue-400" fill="none" viewBox="0 0 24 24" stroke="currentColor"><rect x="3" y="6" width="18" height="12" rx="2" stroke-width="2"/></svg>
                    <span>Button</span>
                  </div>
                  <div class="flex items-center space-x-1.5 px-2 py-1 rounded hover:bg-[#162038]/50 text-slate-400">
                    <svg class="w-3 h-3 text-slate-500" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h8m-8 6h16"/></svg>
                    <span>Label</span>
                  </div>
                  <div class="flex items-center space-x-1.5 px-2 py-1 rounded hover:bg-[#162038]/50 text-slate-400">
                    <svg class="w-3 h-3 text-slate-500" fill="none" viewBox="0 0 24 24" stroke="currentColor"><rect x="3" y="3" width="18" height="18" rx="2" stroke-width="2"/></svg>
                    <span>Layout</span>
                  </div>
                  <div class="flex items-center space-x-1.5 px-2 py-1 rounded hover:bg-[#162038]/50 text-slate-400">
                    <svg class="w-3 h-3 text-purple-400" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"/></svg>
                    <span>AI Copilot</span>
                  </div>
                </div>
              </div>

              <!-- Component Hierarchy Tree -->
              <div class="border-t border-slate-700/50 pt-2">
                <div class="text-[9px] font-semibold text-slate-400 uppercase tracking-wider mb-1.5">Hierarchy</div>
                <div class="text-[10px] space-y-0.5 font-mono text-slate-400">
                  <div class="flex items-center text-slate-300">└ Screen1</div>
                  <div class="flex items-center pl-2 text-slate-400">├ HeaderBar</div>
                  <div class="flex items-center pl-2 text-blue-400 bg-blue-500/10 px-1 rounded">└ TrackBtn</div>
                </div>
              </div>
            </div>

            <!-- 2. Center Mobile Device Canvas (Col 6) -->
            <div class="col-span-6 bg-[#0B111F] p-3 flex flex-col items-center justify-center relative overflow-hidden">
              
              <!-- Subtle Canvas Grid -->
              <div class="absolute inset-0 dev-grid opacity-15 pointer-events-none"></div>

              <!-- Mobile Phone Mockup -->
              <div class="w-[185px] h-[260px] bg-slate-900 rounded-[20px] p-2 border-2 border-slate-700/80 shadow-lg flex flex-col relative z-10">
                <!-- Phone Island / Speaker Notch -->
                <div class="w-12 h-2.5 bg-slate-800 rounded-full mx-auto mb-1.5 flex items-center justify-center">
                  <div class="w-1.5 h-1.5 rounded-full bg-slate-700"></div>
                </div>

                <!-- Screen Contents (Realistic Mobile App UI) -->
                <div class="w-full flex-1 bg-white rounded-[12px] p-2 flex flex-col justify-between text-slate-800 overflow-hidden text-[9px]">
                  
                  <!-- Mobile App Header -->
                  <div>
                    <div class="flex items-center justify-between pb-1.5 border-b border-slate-100">
                      <div class="font-semibold text-slate-900 text-[10px]">Track Courier</div>
                      <span class="text-[8px] text-blue-600 font-medium">Live</span>
                    </div>

                    <!-- Delivery Status Card -->
                    <div class="mt-2 bg-slate-50 border border-slate-100 rounded-lg p-1.5">
                      <div class="text-[8px] text-slate-400 font-medium">Estimated Arrival</div>
                      <div class="font-bold text-slate-900 text-[11px] mt-0.5">14 Mins · 2.4 km</div>
                      <div class="w-full bg-slate-200 h-1 rounded-full mt-1.5 overflow-hidden">
                        <div class="bg-blue-600 h-1 rounded-full w-3/4"></div>
                      </div>
                    </div>

                    <!-- Courier Info Row -->
                    <div class="mt-2 flex items-center space-x-2">
                      <div class="w-5 h-5 rounded-full bg-blue-100 text-blue-600 font-bold flex items-center justify-center text-[8px]">
                        DK
                      </div>
                      <div class="leading-tight">
                        <div class="font-semibold text-slate-800 text-[9px]">David K.</div>
                        <div class="text-[7.5px] text-slate-400">Toyota Prius · 4.9★</div>
                      </div>
                    </div>
                  </div>

                  <!-- Primary Action Button Component inside Phone Canvas -->
                  <div>
                    <button class="w-full py-1.5 rounded-md bg-blue-600 text-white font-medium text-[9px] shadow-xs text-center ring-2 ring-blue-400/50">
                      Track Live Location
                    </button>
                  </div>

                </div>
              </div>

              <!-- Docked Visual Logic Block Snippet -->
              <div class="absolute bottom-2 right-2 z-20 bg-[#172238] border border-blue-500/40 rounded px-2 py-1 text-[9px] font-mono text-slate-200 shadow-md flex items-center space-x-1.5">
                <span class="w-1.5 h-1.5 rounded-full bg-blue-400"></span>
                <span>when <strong class="text-blue-300">TrackBtn</strong>.Click do</span>
              </div>

            </div>

            <!-- 3. Right Property Inspector & AI Helper (Col 3) -->
            <div class="col-span-3 border-l border-slate-700/60 bg-[#0E1526] p-2.5 flex flex-col justify-between">
              <div>
                <div class="text-[9px] font-semibold text-slate-400 uppercase tracking-wider mb-2">Properties</div>
                <div class="text-[10px] space-y-2">
                  <div>
                    <label class="text-[8.5px] text-slate-400 block mb-0.5">Component</label>
                    <div class="font-medium text-slate-200 bg-[#162038] px-2 py-1 rounded border border-slate-700/50">TrackBtn</div>
                  </div>
                  <div>
                    <label class="text-[8.5px] text-slate-400 block mb-0.5">Button Text</label>
                    <div class="text-slate-300 bg-[#162038] px-2 py-1 rounded border border-slate-700/50 font-mono text-[9px]">"Track Live..."</div>
                  </div>
                  <div>
                    <label class="text-[8.5px] text-slate-400 block mb-0.5">Background</label>
                    <div class="flex items-center space-x-1.5 bg-[#162038] px-2 py-1 rounded border border-slate-700/50">
                      <span class="w-2.5 h-2.5 rounded bg-blue-600 shrink-0"></span>
                      <span class="font-mono text-[9px] text-slate-300">#2563EB</span>
                    </div>
                  </div>
                </div>
              </div>

              <!-- AI Copilot Suggestion Box -->
              <div class="bg-[#141E33] border border-blue-500/30 rounded p-2 text-[9px]">
                <div class="flex items-center space-x-1 text-blue-400 font-semibold mb-0.5">
                  <span>✦ AI Assistant</span>
                </div>
                <p class="text-slate-400 text-[8.5px] leading-tight">
                  Auto-bound click event to live geolocation service.
                </p>
              </div>
            </div>

          </div>

        </div>
      </div>

      <!-- Right Column Footer: Key Technical Attributes -->
      <div class="relative z-10 flex items-center justify-between text-xs text-slate-400 border-t border-slate-800/80 pt-4">
        <span class="hover:text-slate-300 transition">Visual Block Logic</span>
        <span class="text-slate-700">·</span>
        <span class="hover:text-slate-300 transition">AI Code Generation</span>
        <span class="text-slate-700">·</span>
        <span class="hover:text-slate-300 transition">Real-time Mobile Preview</span>
      </div>

    </div>

    <!-- Fullscreen Loading Transition Overlay with mobile-apps.gif -->
    <div id="cresuit-loading-overlay" class="fixed inset-0 z-50 flex flex-col items-center justify-center bg-slate-950/80 backdrop-blur-md opacity-0 pointer-events-none transition-opacity duration-300">
      <div class="flex flex-col items-center max-w-sm px-6 text-center">
        <div class="w-32 h-32 sm:w-36 sm:h-36 rounded-2xl overflow-hidden shadow-2xl border border-slate-700/60 mb-5 bg-slate-900">
          <img src="/static/images/mobile-apps.gif" alt="CreSuit Loading" class="w-full h-full object-cover" onerror="this.src='/mobile-apps.gif';" />
        </div>
        <div class="inline-flex items-center space-x-2 px-2.5 py-0.5 rounded-full bg-blue-500/10 border border-blue-500/20 text-[11px] font-medium text-blue-400 mb-2">
          <span class="w-1.5 h-1.5 rounded-full bg-blue-400 animate-pulse"></span>
          <span>CreSuit Studio</span>
        </div>
        <h3 id="loading-title-text" class="text-lg sm:text-xl font-semibold text-white tracking-tight">Entering CreSuit Studio...</h3>
        <p id="loading-desc-text" class="text-xs sm:text-sm text-slate-400 mt-1">Opening your visual builder and workspace</p>
        <div class="w-40 h-1 bg-slate-800 rounded-full mt-5 overflow-hidden">
          <div class="w-full h-full bg-blue-500 rounded-full animate-[pulse_1.5s_infinite]"></div>
        </div>
      </div>
    </div>

    <!-- ================= FIREBASE AUTH & UI INTERACTION SCRIPTS ================= -->
    <script>
      // Loading Overlay Transition Trigger
      function triggerLoadingTransition(title, subtitle, onComplete) {
        const overlay = document.getElementById('cresuit-loading-overlay');
        const titleEl = document.getElementById('loading-title-text');
        const descEl = document.getElementById('loading-desc-text');
        if (titleEl && title) titleEl.innerText = title;
        if (descEl && subtitle) descEl.innerText = subtitle;
        if (overlay) {
          overlay.classList.remove('opacity-0', 'pointer-events-none');
          overlay.classList.add('opacity-100');
        }
        if (typeof onComplete === 'function') {
          setTimeout(onComplete, 600);
        }
      }

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
          alertBox.classList.remove('hidden', 'bg-rose-50', 'border-rose-200', 'text-rose-800', 
                                   'bg-emerald-50', 'border-emerald-200', 'text-emerald-800', 
                                   'bg-blue-50', 'border-blue-200', 'text-blue-800');
          if (alertIcon) {
            alertIcon.classList.remove('text-rose-600', 'text-emerald-600', 'text-blue-600');
          }

          if (type === 'success') {
            alertBox.classList.add('bg-emerald-50', 'border-emerald-200', 'text-emerald-800');
            if (alertIcon) alertIcon.classList.add('text-emerald-600');
          } else if (type === 'info') {
            alertBox.classList.add('bg-blue-50', 'border-blue-200', 'text-blue-800');
            if (alertIcon) alertIcon.classList.add('text-blue-600');
          } else {
            alertBox.classList.add('bg-rose-50', 'border-rose-200', 'text-rose-800');
            if (alertIcon) alertIcon.classList.add('text-rose-600');
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
          if (heading) heading.innerText = "Create an account";
          if (subtitle) subtitle.innerText = "Start building mobile applications visually with AI assistance.";
          if (submitBtnText) submitBtnText.innerText = "Create Account";
          if (promptText) promptText.innerText = "Already have an account? ";
          if (toggleBtn) toggleBtn.innerText = "Sign in";
          if (isRegisterInput) isRegisterInput.value = "true";
          if (viewBadge) viewBadge.innerText = "Register";
          if (registerNotice) registerNotice.classList.remove('hidden');
          if (loginOptionsRow) loginOptionsRow.classList.add('hidden');
        } else {
          if (heading) heading.innerText = "Welcome back";
          if (subtitle) subtitle.innerText = "Sign in to continue building with CreSuit.";
          if (submitBtnText) submitBtnText.innerText = "Sign In";
          if (promptText) promptText.innerText = "Don't have an account? ";
          if (toggleBtn) toggleBtn.innerText = "Create one";
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
        if (btnText) btnText.innerText = "Sending link...";

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
            if (btnText) btnText.innerText = "Send reset link";
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
                btn.innerText = "Resend verification email";
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
              btn.innerText = "Resend verification email";
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
            btn.innerText = "Resend verification email";
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
              showBannerAlert("Google authenticated! Signing in...", "success");
              return result.user.getIdToken().then((idToken) => {
                document.getElementById('firebaseToken').value = idToken;
                if (result.user.email) {
                  document.getElementById('email').value = result.user.email;
                }
                triggerLoadingTransition("Signing in with Google...", "Entering CreSuit Studio...", () => {
                  document.getElementById('loginForm').submit();
                });
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
            showBannerAlert("Google authenticated! Signing in...", "success");
            result.user.getIdToken().then((idToken) => {
              document.getElementById('firebaseToken').value = idToken;
              if (result.user.email) {
                document.getElementById('email').value = result.user.email;
              }
              triggerLoadingTransition("Google authenticated!", "Entering CreSuit Studio...", () => {
                document.getElementById('loginForm').submit();
              });
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
                triggerLoadingTransition("Signing in with Apple...", "Entering CreSuit Studio...", () => {
                  loginForm.submit();
                });
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
          if (submitBtnText) submitBtnText.innerText = isRegister ? "Creating account..." : "Signing in...";

          const authAction = isRegister
            ? firebase.auth().createUserWithEmailAndPassword(email, password)
            : firebase.auth().signInWithEmailAndPassword(email, password);

          authAction
            .then((userCredential) => {
              if (isRegister) {
                // Registration: send verification email and transition to verification screen
                if (userCredential.user) {
                  return userCredential.user.sendEmailVerification()
                    .catch((e) => console.warn("Email verification send warning:", e))
                    .then(() => {
                      return firebase.auth().signOut();
                    })
                    .then(() => {
                      if (submitBtn) submitBtn.disabled = false;
                      if (spinner) spinner.classList.add('hidden');
                      if (submitBtnText) submitBtnText.innerText = "Create Account";
                      switchView('verify', { email: email });
                      showBannerAlert("Account created! A verification link has been sent to " + email + ". Please verify your email before signing in.", "success");
                    });
                }
                return;
              }

              // Login: check if email is verified
              if (userCredential.user && !userCredential.user.emailVerified) {
                userCredential.user.sendEmailVerification().catch(() => {});
                return firebase.auth().signOut().then(() => {
                  if (submitBtn) submitBtn.disabled = false;
                  if (spinner) spinner.classList.add('hidden');
                  if (submitBtnText) submitBtnText.innerText = "Sign In";
                  switchView('verify', { email: email });
                  showBannerAlert("Please verify your email address before signing in. A verification link has been sent to " + email + ".", "info");
                });
              }

              // Email is verified -> proceed with login and loading transition
              return userCredential.user.getIdToken().then((idToken) => {
                firebaseTokenInput.value = idToken;
                triggerLoadingTransition("Signing in...", "Entering CreSuit Studio...", () => {
                  document.getElementById('loginForm').submit();
                });
              });
            })
            .catch((err) => {
              console.warn("Client Firebase Auth failed, falling back to server:", err);
              // If Firebase Auth reported user-not-found on login, prompt user
              if (err.code === 'auth/user-not-found') {
                showBannerAlert("No account found with this email. Click 'Create one' below to register.", "error");
                if (submitBtn) submitBtn.disabled = false;
                if (spinner) spinner.classList.add('hidden');
                if (submitBtnText) submitBtnText.innerText = "Sign In";
                return;
              }
              if (err.code === 'auth/wrong-password') {
                showBannerAlert("Incorrect password. Please try again or click 'Forgot password?'.", "error");
                if (submitBtn) submitBtn.disabled = false;
                if (spinner) spinner.classList.add('hidden');
                if (submitBtnText) submitBtnText.innerText = "Sign In";
                return;
              }
              if (err.code === 'auth/email-already-in-use') {
                showBannerAlert("This email is already registered. Please sign in or reset your password.", "error");
                if (submitBtn) submitBtn.disabled = false;
                if (spinner) spinner.classList.add('hidden');
                if (submitBtnText) submitBtnText.innerText = "Create Account";
                return;
              }
              // Submit to backend fallback
              triggerLoadingTransition("Signing in...", "Entering CreSuit Studio...", () => {
                document.getElementById('loginForm').submit();
              });
            });
        }
      });
    </script>
  </body>
</html>