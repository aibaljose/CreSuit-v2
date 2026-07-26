<%@page import="javax.servlet.http.HttpServletRequest"%>
<%@page import="com.google.appinventor.server.util.UriBuilder"%>
<%@page import="org.apache.commons.lang3.StringEscapeUtils"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!doctype html>
<%
   String error = StringEscapeUtils.escapeHtml4(request.getParameter("error"));
   String useGoogleLabel = (String) request.getAttribute("useGoogleLabel");
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
    <title>MIT App Inventor - Login</title>
    <!-- Tailwind CSS v4 CDN -->
    <script src="https://unpkg.com/@tailwindcss/browser@4"></script>
    <!-- Google Fonts Inter -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <style>
      body {
        font-family: 'Inter', sans-serif;
      }
    </style>
  </head>
  <body class="min-h-screen flex items-center justify-center bg-slate-950 p-4 sm:p-6 md:p-8 relative overflow-hidden antialiased selection:bg-blue-500 selection:text-white">
    
    <!-- Ambient Radial Glows -->
    <div class="absolute top-0 -left-10 w-96 h-96 bg-blue-600 rounded-full mix-blend-multiply filter blur-3xl opacity-20 animate-pulse"></div>
    <div class="absolute -bottom-10 right-10 w-96 h-96 bg-indigo-600 rounded-full mix-blend-multiply filter blur-3xl opacity-25 animate-pulse" style="animation-delay: 2s;"></div>
    <div class="absolute top-1/3 right-1/4 w-80 h-80 bg-violet-600 rounded-full mix-blend-multiply filter blur-3xl opacity-15 animate-pulse" style="animation-delay: 4s;"></div>

    <!-- Main Container Card -->
    <div class="relative w-full max-w-5xl bg-white/95 backdrop-blur-md rounded-3xl shadow-2xl overflow-hidden border border-white/20 grid grid-cols-1 md:grid-cols-12 min-h-[640px] z-10">
      
      <!-- Left panel: Visual & Features (hidden on mobile, visible on desktop) -->
      <div class="hidden md:flex md:col-span-5 bg-gradient-to-br from-blue-600 to-indigo-700 p-8 flex-col justify-between relative overflow-hidden">
        
        <!-- Header Text -->
        <div>
          <h3 class="text-2xl font-extrabold text-white leading-tight">Create your own apps</h3>
          <p class="mt-3 text-blue-100 text-sm font-medium">Build Android and iOS apps easily with MIT App Inventor's block-based programming.</p>
        </div>
        
        <!-- Floating Mockups -->
        <div class="relative w-full h-80 mt-8">
          <!-- Desktop Mockup -->
          <img src="/static/images/ai2-neo-style.png" alt="Designer Mockup" 
               class="absolute w-[95%] right-[-10%] top-6 rounded-xl shadow-2xl border border-white/10 transform -rotate-3 transition duration-500 hover:rotate-0 hover:scale-105 select-none">
          
          <!-- Phone Mockup -->
          <img src="/static/images/phonePortraitModern.png" alt="Phone Companion" 
               class="absolute w-[40%] left-[-5%] bottom-0 rounded-[2rem] shadow-2xl border-4 border-white/95 transform rotate-6 transition duration-500 hover:rotate-0 hover:scale-105 select-none">
        </div>
        
        <!-- Left Panel Footer -->
        <div class="text-xs text-blue-200/80 font-medium">
          Join millions of developers worldwide.
        </div>
      </div>

      <!-- Right panel: Login Form -->
      <div class="col-span-12 md:col-span-7 bg-white p-8 sm:p-12 flex flex-col justify-between">
        
        <!-- Right Panel Header (Logo & Subtitle) -->
        <div class="flex items-center space-x-3 mb-6">
          <img class="h-10 w-auto" src="/static/images/codi-logo.svg" alt="MIT App Inventor Logo">
          <div>
            <h1 class="text-lg font-bold text-slate-800 tracking-tight leading-none">MIT App Inventor</h1>
            <span class="text-xs font-semibold text-slate-400">App Creation Panel</span>
          </div>
        </div>

        <!-- Form content wrapper -->
        <div class="flex-grow flex flex-col justify-center max-w-md w-full mx-auto">
          
          <!-- Heading -->
          <div>
            <h2 class="text-2xl font-bold text-slate-900">${pleaselogin}</h2>
            <p class="text-sm text-slate-500 mt-1">Welcome back! Please enter your credentials to login.</p>
          </div>

          <!-- Error Alert Banner -->
          <% if (error != null) { %>
          <div class="mt-4 bg-rose-50 border-l-4 border-rose-500 p-4 rounded-r-xl shadow-sm">
            <div class="flex">
              <div class="flex-shrink-0">
                <svg class="h-5 w-5 text-rose-500" viewBox="0 0 20 20" fill="currentColor">
                  <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clip-rule="evenodd" />
                </svg>
              </div>
              <div class="ml-3">
                <p class="text-sm font-semibold text-rose-800"><%= error %></p>
              </div>
            </div>
          </div>
          <% } %>

          <!-- Credentials Form -->
          <form method="POST" action="/login" class="space-y-5 mt-6">
            
            <!-- Email Field -->
            <div>
              <label for="email" class="block text-sm font-semibold text-slate-700 mb-1">
                ${emailAddressLabel}
              </label>
              <div class="mt-1">
                <input id="email" name="email" type="text" autocomplete="email" required placeholder="Enter your email address"
                       class="appearance-none block w-full px-3 py-2.5 border border-slate-200 rounded-lg shadow-sm placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-slate-900 sm:text-sm transition duration-150 ease-in-out bg-slate-50/50">
              </div>
            </div>

            <!-- Password Field -->
            <div>
              <label for="password" class="block text-sm font-semibold text-slate-700 mb-1">
                ${passwordLabel}
              </label>
              <div class="mt-1">
                <input id="password" name="password" type="password" autocomplete="current-password" required placeholder="Enter your password"
                       class="appearance-none block w-full px-3 py-2.5 border border-slate-200 rounded-lg shadow-sm placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-slate-900 sm:text-sm transition duration-150 ease-in-out bg-slate-50/50">
              </div>
            </div>

            <!-- Hidden Params -->
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

            <!-- Submit Button (Blue with indicator arrow) -->
            <div>
              <button type="submit" 
                      class="w-full flex items-center justify-center gap-2 py-2.5 px-4 border border-transparent rounded-lg shadow-lg text-sm font-bold text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 cursor-pointer transition duration-150 ease-in-out transform hover:-translate-y-0.5 active:translate-y-0">
                <span>${login}</span>
                <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M14 5l7 7m0 0l-7 7m7-7H3" />
                </svg>
              </button>
            </div>
          </form>

          <!-- Forgot Password -->
          <div class="mt-4 text-center">
            <a href="/login/sendlink?locale=<%= locale %>" 
               class="text-sm font-semibold text-blue-600 hover:text-blue-500 hover:underline transition duration-150">
              ${passwordclickhereLabel}
            </a>
          </div>

          <!-- Social Google Sign-In -->
          <% if (useGoogleLabel != null && useGoogleLabel.equals("true")) { %>
          <div class="mt-6">
            <div class="relative flex items-center justify-center my-4">
              <div class="absolute inset-0 flex items-center" aria-hidden="true">
                <div class="w-full border-t border-slate-200"></div>
              </div>
              <div class="relative bg-white px-4 text-xs font-semibold text-slate-400 uppercase tracking-wider">
                Or
              </div>
            </div>

            <a href="<%= new UriBuilder("/login/google")
                          .add("locale", locale)
                          .add("autoload", autoload)
                          .add("repo", repo)
                          .add("galleryId", galleryId)
                          .add("ng", newGalleryId)
                          .add("ui", uiPreference)
                          .add("redirect", redirect).build() %>" 
               class="w-full flex items-center justify-center gap-3 py-2.5 px-4 rounded-lg border border-slate-200 bg-white hover:bg-slate-50 text-sm font-semibold text-slate-700 shadow-sm transition duration-150 ease-in-out transform hover:-translate-y-0.5 active:translate-y-0 cursor-pointer">
              <svg class="h-5 w-5" viewBox="0 0 24 24" width="24" height="24" xmlns="http://www.w3.org/2000/svg">
                <path d="M21.35,11.1H12v2.7h5.38C16.88,15.54,14.77,16.5,12,16.5c-3.03,0-5.6-2.05-6.52-4.8c-0.23-0.7-0.37-1.44-0.37-2.2 s0.14-1.5,0.37-2.2c0.92-2.75,3.49-4.8,6.52-4.8c1.65,0,3.13,0.59,4.29,1.71l2.02-2.02C16.51,2.5,14.38,1.5,12,1.5 C7.54,1.5,3.77,4.07,2.02,7.8C1.52,8.87,1.25,10.05,1.25,11.3s0.27,2.43,0.77,3.5c1.75,3.73,5.52,6.3,9.98,6.3 c4.67,0,8.44-3.08,9.75-7.2c0.23-0.7,0.37-1.44,0.37-2.2C22.12,11.45,21.87,11.1,21.35,11.1z" fill="#4285F4"/>
              </svg>
              <span>Login with Google</span>
            </a>
          </div>
          <% } %>
        </div>

        <!-- Right Panel Footer (Languages & Metadata) -->
        <div class="mt-8 pt-6 border-t border-slate-100 flex flex-col space-y-4">
          
          <!-- Languages List -->
          <div class="flex justify-center items-center space-x-1 text-xs font-semibold text-slate-500">
            <a href="<%= new UriBuilder("/login").add("locale", "zh_CN").add("repo", repo).add("autoload", autoload).add("galleryId", galleryId).add("ui", uiPreference).add("redirect", redirect).build() %>" 
               class="px-2.5 py-1 rounded-md hover:bg-slate-100 hover:text-slate-900 transition <%= locale.equals("zh_CN") ? "bg-slate-100 text-slate-900" : "" %>">中文</a>
            <span class="text-slate-200">|</span>
            <a href="<%= new UriBuilder("/login").add("locale", "pt").add("repo", repo).add("autoload", autoload).add("galleryId", galleryId).add("ui", uiPreference).add("redirect", redirect).build() %>" 
               class="px-2.5 py-1 rounded-md hover:bg-slate-100 hover:text-slate-900 transition <%= locale.equals("pt") ? "bg-slate-100 text-slate-900" : "" %>">Português</a>
            <span class="text-slate-200">|</span>
            <a href="<%= new UriBuilder("/login").add("locale", "en").add("repo", repo).add("autoload", autoload).add("galleryId", galleryId).add("ng", newGalleryId).add("ui", uiPreference).add("redirect", redirect).build() %>" 
               class="px-2.5 py-1 rounded-md hover:bg-slate-100 hover:text-slate-900 transition <%= locale.equals("en") ? "bg-slate-100 text-slate-900" : "" %>">English</a>
          </div>

          <!-- Bottom Logos and Licensing Info -->
          <div class="flex flex-wrap justify-between items-center text-[10px] text-slate-400 gap-2">
            <div class="flex items-center space-x-2">
              <a rel="license" href="http://creativecommons.org/licenses/by-sa/3.0/" target="_blank" class="hover:opacity-100 opacity-60 transition">
                <img alt="Creative Commons License" src="/static/images/cc3.png" class="h-4">
              </a>
              <span>CC BY-SA 3.0</span>
            </div>
            
            <div class="flex items-center space-x-3">
              <% if (locale != null && locale.equals("zh_CN")) { %>
              <a href="http://www.weibo.com/mitappinventor" target="_blank" class="hover:opacity-100 opacity-60 transition">
                <img class="h-5 w-5 rounded-full" src="/static/images/mzl.png" title="Sina WeiBo" alt="Sina WeiBo">
              </a>
              <% } %>
              <a href="http://www.appinventor.mit.edu" target="_blank" class="hover:opacity-100 opacity-60 transition">
                <img class="h-5 w-auto" src="/static/images/login-app-inventor.jpg" title="MIT App Inventor" alt="MIT App Inventor Logo">
              </a>
            </div>
          </div>

        </div>

      </div>

    </div>
    
  </body>
</html>
