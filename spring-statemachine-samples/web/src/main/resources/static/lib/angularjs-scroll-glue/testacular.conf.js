// Testacular configuration
// Generated on Mon Dec 10 2012 20:57:43 GMT+0100 (W. Europe Standard Time)


// base path, that will be used to resolve files and exclude
basePath = '';


// list of files / patterns to load in the browser
files = [
  JASMINE,
  JASMINE_ADAPTER,
  'test/lib/*.js',
  'https://ajax.googleapis.com/ajax/libs/jquery/1.8.3/jquery.js',
  'https://ajax.googleapis.com/ajax/libs/angularjs/1.0.1/angular.js',
  'https://ajax.googleapis.com/ajax/libs/angularjs/1.0.1/angular-mocks.js',
  'src/scrollglue.js',
  'test/unit/*.spec.js'
];


// list of files to exclude
exclude = [
  
];


// test results reporter to use
// possible values: 'dots', 'progress', 'junit'
reporters = ['progress'];


// web server port
port = 8080;


// cli runner port
runnerPort = 9100;


// enable / disable colors in the output (reporters and logs)
colors = true;


// level of logging
// possible values: LOG_DISABLE || LOG_ERROR || LOG_WARN || LOG_INFO || LOG_DEBUG
logLevel = LOG_INFO;


// enable / disable watching file and executing tests whenever any file changes
autoWatch = true;


// Start these browsers, currently available:
// - Chrome
// - ChromeCanary
// - Firefox
// - Opera
// - Safari (only Mac)
// - PhantomJS
// - IE (only Windows)
browsers = ['Chrome'];


// If browser does not capture in given timeout [ms], kill it
captureTimeout = 5000;


// Continuous Integration mode
// if true, it capture browsers, run tests and exit
singleRun = false;
