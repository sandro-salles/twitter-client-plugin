var exec = require('cordova/exec');

var TwitterClient = {
	login: function (successCallback, errorCallback) {
		exec(successCallback, errorCallback, 'TwitterClient', 'login', []);
	},
	logout: function (successCallback, errorCallback) {
		exec(successCallback, errorCallback, 'TwitterClient', 'logout', []);
	}
};

module.exports = TwitterClient;