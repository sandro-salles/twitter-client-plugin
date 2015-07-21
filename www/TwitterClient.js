var exec = require('cordova/exec');

var TwitterClient = {
	login: function (successCallback, errorCallback) {
		exec(successCallback, errorCallback, 'TwitterClient', 'login', []);
	},
	getFriendsList: function (successCallback, errorCallback) {
		exec(successCallback, errorCallback, 'TwitterClient', 'getFriendsList', []);
	},
	updateStatus: function (statusText, successCallback, errorCallback, options) {
		exec(successCallback, errorCallback, 'TwitterClient', 'updateStatus', [statusText, options]);
	},
	logout: function (successCallback, errorCallback) {
		exec(successCallback, errorCallback, 'TwitterClient', 'logout', []);
	}
};

module.exports = TwitterClient;