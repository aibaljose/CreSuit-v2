// -*- mode: javascript; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0

(function() {
  'use strict';

  // 1. Sidebar Management
  function injectSidebarIfNeeded() {
    var flexColumn = document.querySelector('.ode-Project-FlexColumn');
    if (!flexColumn) return;

    if (document.getElementById('cresuit-persistent-sidebar')) return;

    // Create persistent sidebar
    var sidebar = document.createElement('div');
    sidebar.id = 'cresuit-persistent-sidebar';
    sidebar.className = 'cresuit-sidebar w-[230px] h-full bg-white border-r border-slate-200/90 flex flex-col justify-between shrink-0 select-none z-10 transition-all duration-200';
    sidebar.innerHTML = [
      '<div class="p-3.5 flex flex-col gap-1">',
      '  <div class="sidebar-section-title px-2.5 pt-1.5 pb-2 mb-1 flex items-center justify-between text-[10.5px] font-semibold text-slate-400 uppercase tracking-wider">',
      '    <span class="sidebar-header-text">Navigation</span>',
      '    <span class="sidebar-collapse-trigger material-icons text-sm text-slate-400 hover:text-slate-600 transition-colors cursor-pointer" title="Toggle collapse">menu_open</span>',
      '  </div>',
      '  <!-- 1. Dashboard -->',
      '  <div class="sidebar-nav-item sidebar-item-dashboard flex items-center gap-3 px-3 py-2.5 rounded-xl text-xs font-medium text-slate-600 hover:text-slate-900 hover:bg-slate-50 transition-colors cursor-pointer" title="Dashboard">',
      '    <span class="material-icons text-lg text-slate-400 shrink-0">space_dashboard</span>',
      '    <span class="sidebar-label">Dashboard</span>',
      '  </div>',
      '  <!-- 2. Account -->',
      '  <div class="sidebar-nav-item sidebar-item-account flex items-center gap-3 px-3 py-2.5 rounded-xl text-xs font-medium text-slate-600 hover:text-slate-900 hover:bg-slate-50 transition-colors cursor-pointer" title="Account">',
      '    <span class="material-icons text-lg text-slate-400 shrink-0">person_outline</span>',
      '    <span class="sidebar-label">Account</span>',
      '  </div>',
      '  <!-- 3. Projects (Active State) -->',
      '  <div class="sidebar-nav-item sidebar-item-projects active flex items-center gap-3 px-3 py-2.5 rounded-xl text-xs font-semibold text-blue-600 bg-blue-50/90 border border-blue-100/90 shadow-2xs cursor-pointer" title="Projects">',
      '    <span class="material-icons text-lg text-blue-600 shrink-0">folder_open</span>',
      '    <span class="sidebar-label font-semibold text-blue-700">Projects</span>',
      '  </div>',
      '  <!-- 4. Documentation -->',
      '  <div class="sidebar-nav-item sidebar-item-docs flex items-center gap-3 px-3 py-2.5 rounded-xl text-xs font-medium text-slate-600 hover:text-slate-900 hover:bg-slate-50 transition-colors cursor-pointer" title="Documentation">',
      '    <span class="material-icons text-lg text-slate-400 shrink-0">menu_book</span>',
      '    <span class="sidebar-label">Documentation</span>',
      '  </div>',
      '  <!-- 5. Help -->',
      '  <div class="sidebar-nav-item sidebar-item-help flex items-center gap-3 px-3 py-2.5 rounded-xl text-xs font-medium text-slate-600 hover:text-slate-900 hover:bg-slate-50 transition-colors cursor-pointer" title="Help">',
      '    <span class="material-icons text-lg text-slate-400 shrink-0">help_outline</span>',
      '    <span class="sidebar-label">Help</span>',
      '  </div>',
      '</div>',
      '<!-- Bottom Status Card -->',
      '<div class="sidebar-footer p-3.5 border-t border-slate-100">',
      '  <div class="sidebar-footer-card p-3 rounded-xl bg-slate-50 border border-slate-200/70 text-[11px] text-slate-500">',
      '    <div class="flex items-center justify-between mb-1">',
      '      <span class="font-semibold text-slate-700">CreSuit Cloud</span>',
      '      <span class="inline-block w-2 h-2 rounded-full bg-emerald-500"></span>',
      '    </div>',
      '    <div class="text-[10px] text-slate-400">Workspace Connected</div>',
      '  </div>',
      '</div>'
    ].join('\n');

    // Insert as first child of flexColumn
    flexColumn.style.display = 'flex';
    flexColumn.style.flexDirection = 'row';
    flexColumn.style.width = '100%';
    flexColumn.style.height = '100%';
    flexColumn.style.overflow = 'hidden';

    // Wrap remaining children in a main content container if not already wrapped
    var mainContainer = document.getElementById('cresuit-projects-main-container');
    if (!mainContainer) {
      mainContainer = document.createElement('div');
      mainContainer.id = 'cresuit-projects-main-container';
      mainContainer.className = 'flex-1 h-full flex flex-col overflow-hidden bg-slate-50/70';
      while (flexColumn.firstChild) {
        mainContainer.appendChild(flexColumn.firstChild);
      }
      flexColumn.appendChild(sidebar);
      flexColumn.appendChild(mainContainer);
    } else {
      flexColumn.insertBefore(sidebar, mainContainer);
    }

    // Bind sidebar clicks
    var collapseTrigger = sidebar.querySelector('.sidebar-collapse-trigger');
    if (collapseTrigger) {
      collapseTrigger.addEventListener('click', function(e) {
        e.stopPropagation();
        sidebar.classList.toggle('collapsed');
      });
    }

    var dashboardItem = sidebar.querySelector('.sidebar-item-dashboard');
    if (dashboardItem) {
      dashboardItem.addEventListener('click', function() {
        if (window.Ode && window.Ode.getInstance) {
          window.Ode.getInstance().switchToProjectsView();
        }
      });
    }

    var docsItem = sidebar.querySelector('.sidebar-item-docs');
    if (docsItem) {
      docsItem.addEventListener('click', function() {
        window.open('http://appinventor.mit.edu/explore/ai2/tutorials', '_blank');
      });
    }

    var helpItem = sidebar.querySelector('.sidebar-item-help');
    if (helpItem) {
      helpItem.addEventListener('click', function() {
        var helpBtn = document.querySelector('.ode-TopPanel [name="Help"]');
        if (helpBtn) helpBtn.click();
      });
    }

    var accountItem = sidebar.querySelector('.sidebar-item-account');
    if (accountItem) {
      accountItem.addEventListener('click', function() {
        var accBtn = document.querySelector('.ode-TopPanel .ode-TopPanelLinks button:last-child');
        if (accBtn) accBtn.click();
      });
    }
  }

  // 2. Search Bar in Project Toolbar
  function injectSearchBarIfNeeded() {
    var toolbar = document.querySelector('.ya-ProjectToolbar');
    if (!toolbar) return;

    if (document.getElementById('cresuit-project-search-input')) return;

    var searchContainer = document.createElement('div');
    searchContainer.className = 'cresuit-project-search-bar flex items-center gap-2 bg-slate-50/80 border border-slate-200/90 rounded-xl px-3 py-1.5 w-60 transition-all focus-within:bg-white focus-within:border-blue-500 focus-within:ring-2 focus-within:ring-blue-500/10 shadow-2xs';
    searchContainer.innerHTML = [
      '<span class="material-icons text-sm text-slate-400">search</span>',
      '<input type="text" id="cresuit-project-search-input" placeholder="Search projects..." class="bg-transparent border-none outline-none text-xs text-slate-700 w-full placeholder-slate-400" />'
    ].join('');

    toolbar.appendChild(searchContainer);

    var searchInput = searchContainer.querySelector('input');
    if (searchInput) {
      searchInput.addEventListener('input', function(e) {
        var query = e.target.value.toLowerCase().trim();
        var cards = document.querySelectorAll('.ode-ProjectRowList > div');
        cards.forEach(function(card) {
          var name = (card.querySelector('.ode-ProjectNameLabel') || card.querySelector('.gwt-Label') || card).textContent.toLowerCase();
          if (!query || name.indexOf(query) !== -1) {
            card.style.display = '';
          } else {
            card.style.display = 'none';
          }
        });
      });
    }
  }

  // 3. Project Cards Polish
  function polishProjectCards() {
    var cards = document.querySelectorAll('.ode-ProjectRowList > div');
    cards.forEach(function(card) {
      if (card.getAttribute('data-cresuit-styled') === 'true') return;
      card.setAttribute('data-cresuit-styled', 'true');

      card.classList.add('cresuit-project-card');

      // Check if duplicate button exists, otherwise add it to the footer action group
      var footer = card.querySelector('.ode-ProjectRowList > div > div:last-child') || card.lastElementChild;
      if (footer) {
        var actionGroup = footer.querySelector('div:last-child');
        if (actionGroup && !actionGroup.querySelector('.cresuit-duplicate-action-btn')) {
          var dupBtn = document.createElement('div');
          dupBtn.className = 'cresuit-duplicate-action-btn cresuit-card-action-btn w-8 h-8 rounded-xl border border-slate-200 flex items-center justify-center text-slate-600 hover:bg-slate-100 hover:text-blue-600 transition cursor-pointer shadow-2xs';
          dupBtn.title = 'Duplicate project';
          dupBtn.innerHTML = '<span class="material-icons text-base">content_copy</span>';
          dupBtn.addEventListener('click', function(ev) {
            ev.stopPropagation();
            var nameEl = card.querySelector('.ode-ProjectNameLabel') || card.querySelector('.gwt-Label');
            var projName = nameEl ? nameEl.textContent.trim() : 'Project';
            var newName = prompt('Enter a new name for the duplicated project:', projName + '_copy');
            if (newName && newName.trim()) {
              // Trigger project selection and SaveAs command
              var chk = card.querySelector('input[type="checkbox"]');
              if (chk && !chk.checked) chk.click();
              if (window.Ode && window.Ode.getInstance) {
                // If ProjectNode is accessible
                var projList = window.Ode.getInstance().getProjectManager();
                if (projList) {
                  // Fallback: SaveAs action
                  new com.google.appinventor.client.actions.SaveAsAction().execute();
                }
              }
            }
          });

          // Insert before delete button if available
          var delBtn = actionGroup.querySelector('[title="Delete"]') || actionGroup.lastElementChild;
          if (delBtn) {
            actionGroup.insertBefore(dupBtn, delBtn);
          } else {
            actionGroup.appendChild(dupBtn);
          }
        }
      }
    });
  }

  // Poller / Mutation Observer
  function runLoop() {
    injectSidebarIfNeeded();
    injectSearchBarIfNeeded();
    polishProjectCards();
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', function() {
      setInterval(runLoop, 300);
    });
  } else {
    setInterval(runLoop, 300);
  }

})();
