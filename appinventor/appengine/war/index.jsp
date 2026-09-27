<%@page import="com.google.appinventor.server.Server,com.google.appinventor.common.version.AppInventorFeatures,msg.i18n" %>
<%@page import="com.google.appinventor.server.flags.Flag" %>
<%@page import="com.google.appinventor.server.OdeAuthFilter" %>
<%
  if (request.getScheme().equals("http") && Server.isProductionServer()
      && AppInventorFeatures.enableHttpRedirect()) {
       String qs = request.getQueryString();
       String host = request.getServerName();
       if (qs != null) {
          String redirect = "https://" + host + "/?" + qs;
          response.sendRedirect(redirect);
       } else {
          String redirect = "https://" + host;
          response.sendRedirect(redirect);
       }
    return;
  }
  if (AppInventorFeatures.enableHttpRedirect()) {
      response.setHeader("Strict-Transport-Security", "max-age=3600");
  }
  if (OdeAuthFilter.getUserInfo(request) == null) {
      String qs = request.getQueryString();
      if (qs != null) {
         String redirect = "/login?" + qs;
         response.sendRedirect(redirect);
      } else {
         String redirect = "/login";
         response.sendRedirect(redirect);
      }
      return;
  }
  final String gatag = Flag.createFlag("google.analytics", "").get();
  String isRelease = "@blocklyeditor_isRelease@";
  String cachePostfix = "true".equals(isRelease) ? "cache" : "nocache";
  String blocklyChecksum = "@blocklyeditor_BlocklyChecksum@";
  if (blocklyChecksum.startsWith("@") || blocklyChecksum.isEmpty()) {
    blocklyChecksum = "0.nocache";
  }
  String locale = request.getParameter("locale");
  if (locale == null || locale.isEmpty() || !i18n.mapping.containsKey(locale)) {
    locale = "en";
  }
  String hash = i18n.mapping.getOrDefault(locale, "");
  if (!hash.isEmpty()) {
    hash = "_" + hash;
  }
  final String odeBase = Flag.createFlag("ode.base", "").get();
  String translation = odeBase + "ode/messages" + hash + "." + cachePostfix + ".js";
%>
<!-- Copyright 2007-2009 Google Inc. All Rights Reserved. -->
<!-- Copyright 2011-2025 Massachusetts Institute of Technology. All Rights Reserved. -->
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta http-equiv="X-UA-Compatible" content="IE=10">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <!-- Favicon -->
    <link rel="icon" type="image/png" href="https://aibaljose.github.io/CreSuit/img/play_store_512.png">
    <link rel="shortcut icon" type="image/png" href="https://aibaljose.github.io/CreSuit/img/play_store_512.png">
    <!--meta name="gwt:property" content="locale=en_US"-->
    <!-- Title is set at runtime. -->
    <title> </title>
    <!-- Tailwind CSS v3 CDN (preflight disabled to preserve existing Ya.css styles) -->
    <script src="https://cdn.tailwindcss.com"></script>
    <script>
      tailwind.config = {
        corePlugins: {
          preflight: false,
        }
      }
    </script>
    <% if (!gatag.isEmpty()) { %>
    <!-- Google Analytics. -->
    <!-- Google tag (gtag.js) -->
    <script async src="https://www.googletagmanager.com/gtag/js?id=<%= gatag %>"></script>
    <script>
      window.dataLayer = window.dataLayer || [];
      function gtag(){dataLayer.push(arguments);}
      gtag('js', new Date());

      gtag('config', '<%= gatag %>');
    </script>
<% } %>
    <link type="text/css" rel="stylesheet" href="static/css/gwt.css">
    <link type="text/css" rel="stylesheet" href="static/css/blockly.css">
    <link type="text/css" rel="stylesheet" href="static/css/ai2blockly.css">
    <link type="text/css" rel="stylesheet" href="static/font-awesome/css/font-awesome.min.css">
    <link type="text/css" rel="stylesheet" href="static/leaflet/leaflet.css">
    <link type="text/css" rel="stylesheet" href="static/leaflet/leaflet.toolbar.css">
    <link type="text/css" rel="stylesheet" href="static/leaflet/leaflet-vector-markers.css">
    <link type="text/css" rel="stylesheet" href="static/css/Ya.css">
    <link type="text/css" rel="stylesheet" href="static/css/android_holo.css">
    <link type="text/css" rel="stylesheet" href="static/css/android_material.css">
    <link type="text/css" rel="stylesheet" href="static/css/iOS.css">
    <link type="text/css" rel="stylesheet" href="static/css/DarkTheme.css">
    <link type="text/css" rel="stylesheet" href="static/css/fonts.css">
    <link type="text/css" rel="stylesheet" href="static/css/nano.min.css">
    <noscript>
      <div class="floatingBox">
        <h2> App Inventor needs JavaScript enabled to run.</h2>
      </div>
    </noscript>
    <script type="text/javascript" src="<%=translation%>"></script>
  </head>

  <!-- ODE scripts -->
  <body class="gwt-bodyRob">
    <!-- CreSuit Main Workspace Loading Screen -->
    <div id="cresuit-app-loader" style="position: fixed; top: 0; left: 0; width: 100vw; height: 100vh; z-index: 999999; display: flex; flex-direction: column; align-items: center; justify-content: center; background-color: #0b1120; color: #ffffff; font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; transition: opacity 0.35s ease, visibility 0.35s ease;">
      <div style="display: flex; flex-direction: column; align-items: center; text-align: center; max-width: 420px; padding: 24px;">
        <div style="width: 130px; height: 130px; border-radius: 20px; overflow: hidden; box-shadow: 0 20px 40px -10px rgba(0, 0, 0, 0.7); border: 1px solid rgba(255, 255, 255, 0.12); margin-bottom: 20px; background-color: #111827;">
          <img src="/static/images/mobile-apps.gif" alt="CreSuit Loading" style="width: 100%; height: 100%; object-fit: cover;" onerror="this.src='/mobile-apps.gif';" />
        </div>
        <div style="display: inline-flex; align-items: center; gap: 6px; padding: 3px 10px; border-radius: 9999px; background-color: rgba(59, 130, 246, 0.1); border: 1px solid rgba(59, 130, 246, 0.2); font-size: 11px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.05em; color: #60a5fa; margin-bottom: 10px;">
          <span style="width: 6px; height: 6px; border-radius: 9999px; background-color: #3b82f6;"></span>
          <span>CreSuit Studio</span>
        </div>
        <h2 style="font-size: 20px; font-weight: 600; color: #f8fafc; margin: 0 0 6px 0; letter-spacing: -0.02em;">Loading your workspace...</h2>
        <p style="font-size: 13px; color: #94a3b8; margin: 0; line-height: 1.5;">Initializing visual components and designer canvas</p>
        <div style="width: 160px; height: 3px; background-color: #1e293b; border-radius: 9999px; margin-top: 22px; overflow: hidden; position: relative;">
          <div style="position: absolute; left: 0; top: 0; width: 60%; height: 100%; background: linear-gradient(90deg, #2563eb, #38bdf8); border-radius: 9999px; animation: cresuitBar 1.4s infinite ease-in-out;"></div>
        </div>
      </div>
    </div>
    <style>
      @keyframes cresuitBar {
        0% { left: -60%; }
        100% { left: 100%; }
      }
    </style>
    <script type="text/javascript">
      (function() {
        var loader = document.getElementById('cresuit-app-loader');
        var dismissed = false;
        function hideLoader() {
          if (dismissed || !loader) return;
          dismissed = true;
          loader.style.opacity = '0';
          loader.style.visibility = 'hidden';
          setTimeout(function() {
            if (loader && loader.parentNode) {
              loader.parentNode.removeChild(loader);
            }
          }, 400);
        }
        var checkTimer = setInterval(function() {
          if (document.querySelector('.ode-RootPanel') || document.querySelector('.ode-DeckPanel') || document.querySelector('.ode-Box') || (window.Blockly && window.Blockly.mainWorkspace)) {
            clearInterval(checkTimer);
            hideLoader();
          }
        }, 150);
        setTimeout(function() {
          clearInterval(checkTimer);
          hideLoader();
        }, 15000);
      })();
    </script>
    <div class="floatingBox" style="display:none" id="unsupported">
      <h2> Your browser might not be compatible. </h2>
      To use App Inventor, you must use a compatible browser.<br>
      Currently the supported browsers are:
      <ul>
        <li> Google Chrome 55+ </li>
        <li> Safari 11+ </li>
        <li> Firefox 52+ </li>
      </ul>
    </div>
    <% if (!odeBase.isEmpty()) { %>
    <div id=odeblock style="display: none;">
        <h1>If you see this message for an extended period of time, it might be because
        your internet service is blocking requests to <%= odeBase %>. Contact your
        administrator to check on this and remove the block.
        </h1>
    </div>
    <% } %>
    <script type="text/javascript">
      (function() {
        setTimeout(function() {
          var block = document.getElementById('odeblock');
          if (block) {
            block.style.display = 'block';
          }
        }, 2000);
      })();
    </script>
    <script src="static/js/tutorial.js"></script>
    <script type="text/javascript" src="static/closure-compiler/base.js"></script>
    <script type="text/javascript" src="<%= odeBase %>ode/aiblockly-<%= blocklyChecksum %>.js"></script>
    <script type="text/javascript" src="static/js/scroll-options-6.0.16.min.js"></script>
    <script type="text/javascript" src="static/js/workspace-search-9.1.10.min.js"></script>
    <script type="text/javascript" src="static/js/block-dynamic-connection-0.7.16.min.js"></script>
    <script type="text/javascript" src="static/js/pickr.min.js"></script>
    <script type="text/javascript" src="static/js/workspace-multiselect-pr135-edf5dd2.min.js"></script>
    <script type="text/javascript" src="static/js/keyboard-navigation-0.7.0.min.js"></script>
    <script type="text/javascript" src="<%= odeBase %>ode/cdnok.js"></script>
    <script type="text/javascript" src="static/js/jszip.min.js"></script>
    <script type="text/javascript" src="<%= odeBase %>ode/ode.nocache.js"></script>
    <script type="text/javascript" src="static/js/blockly-field-colour-5.0.19.min.js"></script>
    <script type="text/javascript">
      // Register FieldColour plugin for Blockly v11 compatibility
      if (typeof window.registerFieldColour === 'function') {
        window.registerFieldColour();
      } else if (typeof registerFieldColour === 'function') {
        registerFieldColour();
      }

      // Make FieldColour available as Blockly.FieldColour
      if (typeof window.FieldColour === 'function') {
        Blockly.FieldColour = window.FieldColour;
      }
    </script>
    <script src="static/leaflet/leaflet.js"></script>
    <script src="static/leaflet/leaflet.toolbar.js"></script>
    <script src="static/leaflet/leaflet-vector-markers.min.js"></script>
    <script src="static/leaflet/leaflet-imgicon.js"></script>
    <script src="static/leaflet/Path.Drag.js"></script>
    <script src="static/leaflet/Leaflet.Editable.js"></script>
    <script src="static/leaflet/leaflet.geometryutil.js"></script>
    <script src="static/leaflet/leaflet.snap.js"></script>
    <script>
      if (window.navigator.userAgent.indexOf("MSIE") != -1){
          document.getElementById("unsupported").style.display = 'block';
      }
    </script>
    <script type="text/javascript" src="static/js/cresuit-dashboard.js"></script>
  </body>
</html>
