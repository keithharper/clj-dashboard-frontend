(function() {
  'use strict';

  var app = {
    isLoading: true,
    spinner: document.querySelector('.loader'),
    cardTemplate: document.querySelector('.card-template'),
    container: document.querySelector('.main'),
    sidebarMenuButton: document.querySelector('#menu-button'),
    sidebarMenu: document.querySelector('.sidebar'),
    shutdownButton: document.querySelector('.shutdown-button'),
    wanButton: document.querySelector('.wan-button'),
    uploadButton: document.querySelector('.upload-button'),
    visibleCards: {},
    availableCommands: {},
    loadingQueue: []
  };

  /*****************************************************************************
   *
   * Event listeners for UI elements
   *
   ****************************************************************************/

  app.shutdownButton.addEventListener('click', function() {
    var shouldShutdown = confirm("This will initiate a system shutdown.");

    if(shouldShutdown) {
      app.initiateAdminCommand(app.shutdownButton);
    }
  });

  app.wanButton.addEventListener('click', function() {
    var gigabitConnected = confirm("Make sure to connect the Gigabit port to the network before you continue.")

    if(gigabitConnected) {
      app.initiateAdminCommand(app.wanButton);
    }
  });

  app.uploadButton.addEventListener('click', function() {
    var shouldUpload = confirm("This will toggle the upload status. Make sure the active connection is not cellular before proceeding.")

    if(shouldUpload) {
      app.initiateAdminCommand(app.uploadButton);
    }
  });

  document.querySelector('#refresh-button').addEventListener('click', function() {
    app.refreshCommandOutputs();
  });
  
  document.querySelector('#delete-button').addEventListener('click', function() {
    app.deleteAllVisibleCards();
  });
 
  app.sidebarMenuButton.addEventListener('click', function() {
    var isActive = app.sidebarMenuButton.classList.contains("active");
    app.toggleSidebar(isActive);
  });
  
  app.container.addEventListener('click', function() {
    app.toggleSidebar(true);
  });
  
  app.addSidebarItemButtonListener = function(button) {
    button.addEventListener('click', function(event) {
      var sidebarItem = event.currentTarget.parentNode;
      var command = { "section": sidebarItem.parentNode.id, "command": sidebarItem.textContent, "key": sidebarItem.id };
      var itemIsActive = sidebarItem.classList.contains("active");
      
      itemIsActive ? app.deleteCommandCard(command) : app.getCommandOutput(command, app.updateCommandCard);
    });
  };
  
  app.addSidebarItemSelectionListener = function(item) {
    item.addEventListener('click', function(event) {
      var sidebarItem = event.currentTarget.parentNode;
      var key = sidebarItem.id;
      var commandCard = app.container.querySelector('[id=\'' + key + '\']');
      if(commandCard) {
        var headerOffset = 60;
        commandCard.parentNode.scrollTop = commandCard.offsetTop - headerOffset;
      }              
    });
  };
  
  app.addSidebarSectionListener = function(section) {
    section.addEventListener('click', function(event) {
      var sidebarSection = event.currentTarget;
      var panel = sidebarSection.nextElementSibling;
      panel.classList.toggle("active");
      sidebarSection.classList.toggle("active");
    });
  };


  /*****************************************************************************
   *
   * Methods to update/refresh the UI
   *
   ****************************************************************************/

  app.toggleSidebar = function(sidebarIsActive) {
    if (sidebarIsActive) {
      app.sidebarMenuButton.classList.remove("active");
      app.sidebarMenu.classList.remove("active");
      app.container.classList.remove("active");
    } else {
      app.sidebarMenuButton.classList.add("active");
      app.sidebarMenu.classList.add("active");
      app.container.classList.add("active");
    }
  };

  app.toggleLoadingState = function() {
    app.isLoading = app.loadingQueue.length > 0;
    
    if (app.isLoading) {
      app.spinner.removeAttribute('hidden');
    } else {
      app.spinner.setAttribute('hidden', true);
      app.container.removeAttribute('hidden');
    }
  };
  
  app.toggleSidebarItemActive = function(key) {
    var node = app.sidebarMenu.querySelector('[id=\'' + key + '\']');
    
    if(node.classList.contains("active")) {
      node.classList.remove("active");     
    } else {
      node.classList.add("active");      
    }
  };
  
  app.addRequestToLoadingQueue = function(request) {
      app.loadingQueue.push(request);
      app.toggleLoadingState();
  };
  
  app.removeRequestFromLoadingQueue = function(request) {
    app.loadingQueue = app.loadingQueue.filter(function(availableObject) { return availableObject !== request });
    app.toggleLoadingState();
  };
  
  app.addAvailableCommandsToSidebar = function() {
    app.availableCommands.forEach(function(commandSection) {
      if(commandSection.section !== "Admin") {
        app.addSectionToSidebar(commandSection);
      }
    });
  };
  
  app.addSectionToSidebar = function(commandSection) {
    var sidebarSectionButton = document.createElement("button");
      sidebarSectionButton.classList.add("accordion");
      sidebarSectionButton.classList.add("mdl-button");

      var sidebarSectionButtonArrow = document.createElement("div");
      sidebarSectionButtonArrow.classList.add("sidebar__section__arrow");
      sidebarSectionButton.appendChild(sidebarSectionButtonArrow);

      var sidebarSectionButtonLabel = document.createElement("div");
      sidebarSectionButtonLabel.innerText = commandSection.section;
      sidebarSectionButtonLabel.classList.add("sidebar__section__label");
      sidebarSectionButton.appendChild(sidebarSectionButtonLabel);

      
      
      var sidebarSection = document.createElement("div");
      sidebarSection.id = commandSection.section;
      sidebarSection.classList.add("sidebar__section");
      
      commandSection.commands.forEach(function(commandObject) {        
        var commandNode = document.createElement("div");
        commandNode.innerText = commandObject.command;        
        commandNode.classList.add("command-node");

        var horizontalLineSpan = document.createElement("span");
        horizontalLineSpan.classList.add("plus-horizontal");
        
        var verticalLineSpan = document.createElement("span");
        verticalLineSpan.classList.add("plus-vertical");

        var plusButton = document.createElement("button");
        plusButton.id = "sidebarButton";
        plusButton.classList.add("sidebar-button");
        plusButton.appendChild(horizontalLineSpan);
        plusButton.appendChild(verticalLineSpan);

        var sidebarItem = document.createElement("div");
        sidebarItem.classList.add("sidebar__item");
        sidebarItem.classList.add("mdl-button");
        sidebarItem.id = commandSection.section + '/' + commandObject.command;
        sidebarItem.appendChild(commandNode);
        sidebarItem.appendChild(plusButton);
        
        app.addSidebarItemSelectionListener(commandNode);
        app.addSidebarItemButtonListener(plusButton);        
        
        sidebarSection.appendChild(sidebarItem);
      });

      app.addSidebarSectionListener(sidebarSectionButton);
      app.sidebarMenu.appendChild(sidebarSectionButton);
      app.sidebarMenu.appendChild(sidebarSection);
  };
  
  app.createCommandCard = function(data) {
    var card = app.cardTemplate.cloneNode(true);
    card.classList.remove('card-template');
    card.querySelector('.card__command-name').textContent = data.command;
    card.removeAttribute('hidden');
    card.id = data.key;

    var deleteCardButton = card.querySelector('#card__delete-button');
    deleteCardButton.addEventListener('click', function() {
      app.deleteCommandCard(data);
    });

    app.container.appendChild(card);
    app.addToVisibleCards(data, card);
    app.toggleSidebarItemActive(card.id);
    
    return card;
  };
  
  // Updates a command card with the latest result. If the card
  // doesn't already exist, it's cloned from the template.
  app.updateCommandCard = function(result) {
    var commandLabel = result.item.label;
    var commandOutput = result.output;
    var cardSection = app.visibleCards[result.section];
    var card;
    
    cardSection ? card = cardSection[result.command] : null;
    card ? true : card = app.createCommandCard(result);

    card.querySelector('.card__date').textContent = commandLabel.date;
    card.querySelector('.card__container .output').textContent = commandOutput;
    app.updateCommandCacheWithVisibleCards();
  };
  
  app.deleteCommandCard = function(data) {
    var cardToDelete = app.visibleCards[data.section][data.command];
    
    if(cardToDelete) {
      cardToDelete.remove();
      delete app.visibleCards[data.section][data.command];
      app.deleteSectionIfEmpty(data.section);
      
      app.toggleSidebarItemActive(data.key);
      app.updateCommandCacheWithVisibleCards();
    }
  };
  
  app.deleteAllVisibleCards = function() {
    for(var section in app.visibleCards) {
      var visibleCommands = app.visibleCards[section];
      
      for(var command in visibleCommands) {
        var key = section + '/' + command;
        var commandObject = { "section": section, "command": command, "key": key };
        app.deleteCommandCard(commandObject);
      }      
    }
  };

  app.deleteSectionIfEmpty = function(section) {
    var isEmpty = Object.keys(app.visibleCards[section]).length === 0;
      
    if(isEmpty) {
      delete app.visibleCards[section];
    }
  };

  /*****************************************************************************
   *
   * Methods for dealing with the model
   *
   ****************************************************************************/

  /*
   * Gets the output for an executed command and updates the card with the data.
   */
  app.getCommandOutput = function(commandObject, callback) {
    var commandKey = commandObject.section + '/' + commandObject.command;
    var url = window.location.pathname + '/run/' + commandKey;
    
    var request = new XMLHttpRequest();
    request.onreadystatechange = function() {
      if (request.readyState === XMLHttpRequest.DONE) {
        if (request.status === 200) {
          var response = JSON.parse(request.response);
          var results = response.query;
          results.section = commandObject.section;
          results.key = commandObject.key;
          results.command = commandObject.command;
          
          callback(results);
          app.removeRequestFromLoadingQueue(commandKey);
        }
      }
    };
    request.open('GET', url);
    app.addRequestToLoadingQueue(commandKey);
    
    request.send();
  };
  
  /*
   * Gets a list of available commands, adds them to the command list,
   * and loads previously executed commands from local storage.
   */
  app.getAvailableCommands = function() {
    var url = window.location.pathname + '/commands';

    var request = new XMLHttpRequest();
    request.onreadystatechange = function() {
      if (request.readyState === XMLHttpRequest.DONE) {
        if (request.status === 200) {
          var response = JSON.parse(request.response);
          app.availableCommands = response;
          app.addAvailableCommandsToSidebar();
          app.loadCommandsFromCache();
          app.removeRequestFromLoadingQueue('commands');
        }
      }
    };
    app.addRequestToLoadingQueue('commands');
    
    request.open('GET', url);
    request.send();
  };

  // Iterate all of the cards and attempt to get the latest output data
  app.refreshCommandOutputs = function() {    
    for(var section in app.visibleCards) {
      var visibleCommands = app.visibleCards[section];
      
      for(var command in visibleCommands) {
        var key = section + '/' + command;
        var commandObject = { "section": section, "command": command, "key": key };
        app.getCommandOutput(commandObject, app.updateCommandCard); 
      }      
    }
  };
  
  app.initiateAdminCommand = function(node) {
    var key = node.id;
    var section = key.split('/')[0];
    var command = key.split('/')[1];
    var commandObject = { "section": section, "command": command, "key": key };

    app.getCommandOutput(commandObject, function(result) {
      window.alert(result.output);
    });
  };
 
  app.addToVisibleCards = function(data, card) {
    if(!app.visibleCards[data.section]) {
      app.visibleCards[data.section] = {};
    }
    app.visibleCards[data.section][data.command] = card;
  };
  
  // Update local storage 'commands' item with currently selected commands
  app.updateCommandCacheWithVisibleCards = function() {
    var visibleCards = JSON.stringify(app.visibleCards);
    window.localStorage.setItem('commands', visibleCards);
  };
  
  app.loadCommandsFromCache = function() {
    var localStorage = window.localStorage;
    var cache = localStorage.commands;
    try {
      if(cache !== "{}") {
        app.runValidCachedCommands(JSON.parse(cache));
      } else {
        throw 'Empty JSON';        
      }                          
    } catch(e) {
      app.toggleSidebar();
    }
  };
  
  app.runValidCachedCommands = function(cachedCommandsObject) {
    for(var section in cachedCommandsObject) {
      var availableSection = app.availableCommands.filter(
        function(availableObject) { return availableObject.section === section; }
      );
      
      if(!availableSection) {
        delete cachedCommandsObject[section];
      } else {
        for(var command in cachedCommandsObject[section]) {
          var valid = app.commandExistsInSection(command, availableSection);
          
          if(!valid) {
            delete cachedCommandsObject[section][command];
          } else {
            var key = section + '/' + command;
            var commandObject = { "section": section, "command": command, "key": key };
            
            app.getCommandOutput(commandObject, app.updateCommandCard); 
          }          
        }
      }            
    } 
  };
  
  app.commandExistsInSection = function(command, section) {
    var filteredCommand = section.filter(function(item) {
      var found = item.commands.filter(function(currentCommand) { return currentCommand.command === command; });
      return found.length > 0;
    });

    return filteredCommand.length > 0;
  };
  
  app.getCommandsBySection = function(section) {
    return app.availableCommands.filter(function(data) { return data.section === section; });
  };

  /*****************************************************************************
   *
   * Startup method execution
   *
   ****************************************************************************/
  
   // Fetch available commands and populate list
   app.getAvailableCommands();
})();
