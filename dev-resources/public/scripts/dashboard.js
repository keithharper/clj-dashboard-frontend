//test
(function() {
  'use strict';

  var app = {
    isLoading: true,
    visibleCards: {},
    alertBox: document.querySelector('.alert'),
    spinner: document.querySelector('.loader'),
    cardTemplate: document.querySelector('.card-template'),
    sectionTemplate: document.querySelector('.section-template'),
    container: document.querySelector('.main'),
    dashboardCard: {},
    dashboardCommands: {},
    loadingQueue: [],
    alertQueue: []
  };

  /*****************************************************************************
   *
   * Event listeners for UI elements
   *
   ****************************************************************************/

  document.querySelector('#refresh-button').addEventListener('click', function() {
    app.refreshCommandOutputs();
  });

  app.addStatusClickListener = function(node) {
    node.addEventListener('click', function(event) {
      var sectionCommand = event.srcElement.parentNode.id.split("/");
      var command = {"section": sectionCommand[0], "command": sectionCommand[1], "key": event.id};
      app.getCommandOutput(command);
    });
  };

  /*****************************************************************************
   *
   * Methods to update/refresh the UI
   *
   ****************************************************************************/

  app.toggleLoadingState = function(node, isLoading) {    
    if (isLoading) {
      node.classList.add('card__loading');
      node.classList.remove('card__pass', 'card__fail');
    } else {
      node.classList.remove('card__loading');
    }
  };
  
  app.addRequestToLoadingQueue = function(request) {
      app.loadingQueue.push(request);
      var requestCard = app.visibleCards[request.section][request.command];
      var statusNode = requestCard.querySelector('#status');
      app.toggleLoadingState(statusNode, true);
  };
  
  app.removeRequestFromLoadingQueue = function(request) {
    app.loadingQueue = app.loadingQueue.filter(function(availableObject) { return availableObject !== request });
    //app.loadingQueue = app.loadingQueue.filter((availableObject) => availableObject !== request);
    var requestCard = app.visibleCards[request.section][request.command];
    var statusNode = requestCard.querySelector('#status');
    app.toggleLoadingState(statusNode, false);
  };

  app.toggleAlertState = function() {
    var alertActive = app.alertQueue.length > 0;

    if(alertActive) {
        app.alertBox.innerText = "WARNING: This unit still has unuploaded files on it. Do not run it through EOL test or the files will be lost!"
        app.alertBox.classList.add('active');
    } else {
        app.alertBox.innerText = "";
        app.alertBox.classList.remove('active');
    }
  }

  app.addItemToAlertQueue = function(item) {
    var itemExistsInQueue = app.alertQueue.filter(function(alert) {
        return alert === item;
    }) > 0;

    if(!itemExistsInQueue) {
        app.alertQueue.push(item);
        app.toggleAlertState();
    }
  }

  app.removeItemFromAlertQueue = function(item) {
    app.alertQueue = app.alertQueue.filter(function(alert) {
        return alert !== item;
    });
    app.toggleAlertState();
  }
  
  app.createCardsForDashboardCommands = function() {
    app.dashboardCommands.forEach(function(commandSection) {
      //var sectionCard = app.createCommandCard(commandSection);
      var sectionCard = app.createSection(commandSection);
      var commandSectionNode = sectionCard.querySelector('.card__command-section')
      //commandSectionNode.classList.add("card__command-section");
      
      commandSection.commands.forEach(function(commandObject) {        
        var commandNode = document.createElement("div");
        commandNode.id = commandSection.section + '/' + commandObject.command;
        commandNode.classList.add("card__command-node");

        var commandLabel = document.createElement("div");
        commandLabel.innerText = commandObject.command;
        commandLabel.classList.add("card__command-label");

        var statusIcon = document.createElement("div");
        statusIcon.id = "status";
        statusIcon.classList.add("card__command-status");
        statusIcon.classList.add("card__loading");
        
        statusIcon.appendChild(app.spinner.cloneNode(true));
        statusIcon.querySelector('.loader').classList.add('card__spinner');
        statusIcon.querySelector('.loader').classList.remove('loader');
        app.addStatusClickListener(statusIcon);
        
        commandNode.appendChild(statusIcon);
        commandNode.appendChild(commandLabel);

        commandSectionNode.appendChild(commandNode);
        var visibleCard = { "section": commandSection.section, "command": commandObject.command, "key": commandNode.id };
        app.addToVisibleCards(visibleCard, commandNode);
      });

      sectionCard.appendChild(commandSectionNode)
      app.dashboardCard.querySelector('.section-container').appendChild(sectionCard);
    });
    app.refreshCommandOutputs();
  };

  app.createSection = function(data) {
    var section = app.sectionTemplate.cloneNode(true);
    section.classList.remove('section-template');
    section.querySelector('.card__command-name').textContent = data.section;
    section.removeAttribute('hidden');
    return section;
  }

  app.createCommandCard = function() {
    var card = app.cardTemplate.cloneNode(true);
    card.classList.remove('card-template');
    //card.querySelector('.card__command-name').textContent = data.section;
    card.removeAttribute('hidden');
    card.id = "dashboardCard";
    //card.id = data.section;

    app.container.appendChild(card);    
    return card;
  };
  
  // Updates a command card with the latest result. If the card
  // doesn't already exist, it's cloned from the template.
  app.updateCommandCard = function(result) {
    var commandLabel = result.item.label;
    var cardSection = app.visibleCards[result.section];
    var card;
    
    cardSection ? card = cardSection[result.command] : null;
    card ? true : card = app.createCommandCard(result);

    // Change to root card
    app.dashboardCard.querySelector('.card__date').textContent = commandLabel.date;
    var statusNode = card.querySelector('.card__command-status');
    app.updateNodeStatus(statusNode, result.output.trim(), result.important);
  };

  app.updateNodeStatus = function(node, status, important) {
    switch(status) {
      case "PASS":
        node.classList.add('card__pass');
        if(important) {
            app.removeItemFromAlertQueue(node);
        }
        break;
      case "FAIL":
        node.classList.add('card__fail');
        if(important) {
            app.addItemToAlertQueue(node);
        }
        break;
      default:
        node.classList.add('card__fail');
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
  app.getCommandOutput = function(commandObject) {
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
          
          app.updateCommandCard(results);
          app.removeRequestFromLoadingQueue(commandObject);
        }
      }
    };
    request.open('GET', url);
    app.addRequestToLoadingQueue(commandObject);
    
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
          app.dashboardCommands = response;
          app.createCardsForDashboardCommands();
          app.spinner.setAttribute('hidden', true);
        }
      }
    };
    app.spinner.removeAttribute('hidden');
    
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
        app.getCommandOutput(commandObject); 
      }      
    }
  };
 
  app.addToVisibleCards = function(data, card) {
    if(!app.visibleCards[data.section]) {
      app.visibleCards[data.section] = {};
    }
    app.visibleCards[data.section][data.command] = card;
  };
  
  app.commandExistsInSection = function(command, section) {
    var filteredCommand = section.filter(function(item) {
      var found = item.commands.filter(function(currentCommand) { return currentCommand.command === command; });
      return found.length > 0;
    });

    return filteredCommand.length > 0;
  };
  
  app.getCommandsBySection = function(section) {
    return app.dashboardCommands.filter(function(data) { return data.section === section; });
  };

  /*****************************************************************************
   *
   * Startup method execution
   *
   ****************************************************************************/
  
   // Fetch available commands and populate list
   app.dashboardCard = app.createCommandCard();
   app.getAvailableCommands();
})();
