'use strict';

/* Controllers */

angular.module('springChat.controllers', ['toaster'])
	.controller('ChatController', ['$http', '$scope', '$location', '$interval', 'toaster', 'ChatSocket', function($http, $scope, $location, $interval, toaster, chatSocket) {

		var typing = undefined;

		$scope.uuid = '';
		$scope.participants = [];
		$scope.variables = [];
		$scope.messages = [];
		$scope.newMessage = '';

		$scope.sendEvent = function(event) {
			$http.post('/event', null, {params:{"id": event}}).
				success(function(data) {
				});
		};

		$scope.sendEventAndVariable = function(event) {
			$http.post('/event', null, {params:{"id": event, "testVariable": $scope.testVariable}}).
				success(function(data) {
				});
		};

		$scope.joinEnsemble = function(event) {
			$http.post('/join', null, {}).
				success(function(data) {
				});
		};

		$scope.leaveEnsemble = function(event) {
			$http.post('/leave', null, {}).
				success(function(data) {
				});
		};

		var initStompClient = function() {
			chatSocket.init('/ws');

			chatSocket.connect(function(frame) {

				chatSocket.subscribe("/app/sm.uuid", function(message) {
					$scope.uuid = message.body;
				});

				chatSocket.subscribe("/app/sm.states", function(message) {
					$scope.participants = JSON.parse(message.body);
				});

				chatSocket.subscribe("/topic/sm.states", function(message) {
					$scope.participants = JSON.parse(message.body);
				});

				chatSocket.subscribe("/app/sm.variables", function(message) {
					var parsed = JSON.parse(message.body);
					$scope.variables = []
					angular.forEach(parsed, function(val, key) {
						this.push(key + ' = ' + val)
					}, $scope.variables);
				});

				chatSocket.subscribe("/topic/sm.variables", function(message) {
					var parsed = JSON.parse(message.body);
					$scope.variables = []
					angular.forEach(parsed, function(val, key) {
						this.push(key + ' = ' + val)
					}, $scope.variables);
				});

				chatSocket.subscribe("/topic/sm.message", function(message) {
					$scope.messages.unshift(JSON.parse(message.body));
						});

				chatSocket.subscribe("/user/queue/errors", function(message) {
					toaster.pop('error', "Error", message.body);
						});

			}, function(error) {
				toaster.pop('error', 'Error', 'Connection error ' + error);

				});
		};

		initStompClient();
	}]);
