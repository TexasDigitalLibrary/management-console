/**
 * 
 * created by Daniel Bernstein
 */


$(document).ready(function() {

	var serviceDetailPaneId = "#service-detail-pane";
	var detailPaneId = "#detail-pane";

	
	var centerLayout = $('#page-content').layout({
		west__size:				800
	,   west__paneSelector:     "#services-list-view"
	,   west__onresize:         "servicesListPane.resizeAll"
	,	center__paneSelector:	detailPaneId
	,   center__onresize:       "detailPane.resizeAll"
	});
	
	
	
	var servicesListPaneLayout = {
			north__paneSelector:	".north"
		,   north__size: 			35
		,	center__paneSelector:	".center"
		,   resizable: 				false
		,   slidable: 				false
		,   spacing_open:			0			
		,	togglerLength_open:		0	
	};
			
	var servicesListPane = $('#services-list-view').layout(servicesListPaneLayout);
	
	//detail pane's layout options
	var detailLayoutOptions = {
			north__paneSelector:	".north"
				,   north__size: 			130
				,	center__paneSelector:	".center"
				,   resizable: 				false
				,   slidable: 				false
				,   spacing_open:			0
				,	togglerLength_open:		0
				
	};
	
	
	var detailPane = $(detailPaneId).layout(detailLayoutOptions);

	
	var deploy = function(service, future){
		//alert("ajax call here");
		future.success();
	};

	var undeploy = function(service, future){
		//alert("ajax call here");
		future.success();
	};
	
	
	var resolveServiceCssClass = function(services){
		return "service-replicate";
	};

	
	var loadDeploymentDetail = function(service,deployment){
		
		var serviceDetailPane = $(serviceDetailPaneId).clone();

		
		//set the title of the pane
		$(".service-name", serviceDetailPane.first()).html(service.displayName);
		$(".service-version", serviceDetailPane.first()).html(service.serviceVersion);
		
		var centerPane = $(".center",serviceDetailPane.first());
		centerPane.html("");

		//deploy/undeploy switch definition and bindings
		$(".deploy-switch",serviceDetailPane.first()).onoffswitch({
			   		initialState: "on"
					, onStateClass: "unlocked"
					, onIconClass: "x"
					, offStateClass: "unlocked"
					, offIconClass: "x"
					, onText: "Deploy"
					, offText: "Undeploy"		
		}).bind("turnOn", function(evt, future){
			deploy(service, future);
		}).bind("turnOff", function(evt, future){
			undeploy(service, future);
		});

		getServiceDeploymentConfig(service, deployment, {
			success: function(config){
				var data = new Array();
				for(i = 0; i < config.length; i++){
					data[i] = [config[i].name, config[i].value];
				}			
				
				centerPane.prepend(
						$.fn.create("div")
						.tabularexpandopanel(
							{
							  title: "Details", 
							  data:  data
			                }
						)
					);
				
			},
		});

		
		
		centerPane.append(
			$.fn.create("div")
			.tabularexpandopanel(
				{title: "Configuration", 
				 data: convertUserConfigsToArray(deployment.userConfigs)
                 }
			)
		);
		
		serviceDetailPane = dc.swapDetailPane(serviceDetailPane,detailPaneId, detailLayoutOptions);

	};
	
	
	var convertUserConfigsToArray = function(userConfigs){
		var a = new Array();
		for(u in userConfigs){
			var uc = userConfigs[u];
			a.push([uc.displayName, resolveUserConfigValue(uc)]);
		}
		
		return a;
	};
	
	////////////////////////////////////////////////////////
	///util functions for handling service deployment element ids
	var deriveDeploymentId = function(service, deployment){
		return service.id + "-" + deployment.id;
	};

	var extractServiceId = function(id){
		return id.split("-")[0];
	};

	var extractDeploymentId = function(id){
		return id.split("-")[1];
	};


	var resolveUserConfigValue =  function (uc){
		if(uc.displayValue != undefined){
			return uc.displayValue;
		}else {
			if(uc.options != undefined){
    			var options = uc.options[0];
    			var count =	 0;
    			for(var i = 0; i < options.length; i++){
    				var option = options[i];
    				if(option.selected.toString() == "true"){
    					if(count > 0) value+=", ";
	    				value += option.displayName;
	    				count++;
    				}
    			}
    			
    			return value;
			}else{
				var value = "no value";
			}
		}
	};

	var loadDeployedServiceList = function(services){
		var servicesList = $("#services-list");
		servicesList.selectablelist({selectable: false});
		servicesList.selectablelist("clear");
		var defaultServiceSet = false;
		for(s in services){
			var service = services[s];
			for(d in service.deployments){
				var deployment = service.deployments[d];
				
				var item =  $.fn.create(("tr"))
								.attr("id", deriveDeploymentId(service,deployment))
								.addClass("dc-item")
								.addClass(resolveServiceCssClass(service))
								.append($.fn.create("td").addClass("icon").append($.fn.create("div")))
								.append($.fn.create("td").html(service.displayName + " - " + service.serviceVersion))
								.append($.fn.create("td").html(deployment.hostname))
								.append($.fn.create("td").html(deployment.status));
								
				servicesList.selectablelist('addItem',item,{service:service, deployment:deployment});	   
			
				if(!defaultServiceSet){
					loadDeploymentDetail(service,deployment);
					defaultServiceSet = true;
				}		
			}
		}
		
		//bind for current item change listener
		servicesList.bind("currentItemChanged", function(evt,state){
			var data = state.data;
			loadDeploymentDetail(data.service,data.deployment);
		});

	};
	
	var getDeployedServices = function(callback){
		var services = [
		                
		                {	
		                		id: 0
		                	,   displayName: "Replication Service"
		                	,   serviceName:  "replication-service"
		                	,   serviceVersion: "1.0.0"
		                	,   deployments: [
		                	                  {
		                	                	  	id: "1"
		                	                	,	hostname: "127.0.0.1"
		                	                	, 	status: "OK"
		                	                	,   started: "Jan 1, 1970 00:00:00 UTC"
		                	                	,   userConfigs: [
		                	                	                  {
		                	                	                	  	displayName: "user config name1"
		                	                	                	 ,	options: [
				                	                	                           {
				                	                	                        	   selected: false,
				                	                	                        	   displayName: "option1",
				                	                	                        	   
				                	                	                           },
				                	                	                           {
				                	                	                        	   selected: true,
				                	                	                        	   displayName: "option2",
				                	                	                        	   
				                	                	                           },
				                	                	                          ]
				                	                	                   
		                	                	                  },
			                	                	              {
		                	                	                  		displayName: "user config name2"
			                	                	                 ,	displayValue: "user config value2",
			                	                	               },
		                	                	                  ],
		                	                  
		                	                		
		                	                  }
		                	                 ]
		                },
		                
		                {	
	                		id: 2
	                	,   displayName: "Image Magic"
	                	,   serviceName:  "image-magic-service"
	                	,   serviceVersion: "1.0.0"
	                	,   deployments: [
	                	                  {
	                	                	  	id: "1"
	                	                	,	hostname: "127.0.0.2"
	                	                	, 	status: "OK"
	                	                	,   started: "Jan 6, 1972 00:00:00 UTC"
	                	                	,   userConfigs: [
	                	                	                  {
	                	                	                	  	displayName: "some user config1"
	                	                	                	 ,	options: [
			                	                	                           {
			                	                	                        	   selected: false,
			                	                	                        	   displayName: "suc value 1",
			                	                	                        	   
			                	                	                           },
			                	                	                           {
			                	                	                        	   selected: true,
			                	                	                        	   displayName: "suc value 2",
			                	                	                        	   
			                	                	                           },
			                	                	                          ]
			                	                	                   
	                	                	                  },
		                	                	              {
	                	                	                  		displayName: "user config x name"
		                	                	                 ,	displayValue: "user config x value",
		                	                	               },
	                	                	                  ],
	                	                  
	                	                		
	                	                  }
	                	                 ]
	                },
		                
		                ];
		
		//implement ajax call here and remove the above mock data
		callback.load(services);
	};

	
	var getServiceDeploymentConfig = function(service, deployment, callback){
		//implement ajax call here
		var config = [
		              {name: "name1", value:"value1"},
		              {name: "name2", value:"value2"},
		              {name: "name3", value:"value3"},
		              {name: "name4", value:"value4"},
		              {name: "name5", value:"value5"},
		
		];
		
		callback.success(config);
		
	};
	getDeployedServices({load: loadDeployedServiceList});
	
	
	//dialogs
	
	$('#available-services-dialog').dialog({
		autoOpen: false,
		show: 'blind',
		hide: 'blind',
		resizable: false,
		height: 250,
		closeOnEscape:true,
		modal: true,
		width:500,
		buttons: {
			'Next': function(evt) {
				$(this).dialog("close");
			},
			Cancel: function(evt) {
				$(this).dialog('close');
			}
		},
		
		close: function() {
	
		},
		
		open: function(e){
		
		}
		
	});

	$(".deploy-service-button").click(function(){
		$("#available-services-dialog").dialog("open");
	});

	
	$('#configure-service-dialog').dialog({
		autoOpen: false,
		show: 'blind',
		hide: 'blind',
		resizable: false,
		height: 250,
		closeOnEscape:true,
		modal: true,
		width:500,
		buttons: {
			'< Back': function(evt) {
				$(this).dialog("close");
			},

			'Deploy': function(evt) {
				$(this).dialog("close");
			},
			Cancel: function(evt) {
				$(this).dialog('close');
			}
		},
		
		close: function() {
	
		},
		
		open: function(e){
		
		}
		
	});
	
	$(".configure-service-button").click(function(){
			$("#configure-service-dialog").dialog("open");
	});

	$(".ui-dialog-titlebar").hide();


});