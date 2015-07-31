AngularJS-Toaster
=================

**AngularJS Toaster** is an AngularJS port of the **toastr** non-blocking notification jQuery library. It requires AngularJS v1.2.6 or higher and angular-animate for the CSS3 transformations. 
(I would suggest to use /1.2.8/angular-animate.js, there is a weird blinking in newer versions.)

### Current Version 0.4.15

## Demo
- Simple demo is at http://plnkr.co/edit/HKTC1a
- Older versions are http://plnkr.co/edit/1poa9A or http://plnkr.co/edit/4qpHwp or http://plnkr.co/edit/lzYaZt (with version 0.4.5)
- Older version with Angular 1.2.0 is placed at http://plnkr.co/edit/mejR4h
- Older version with Angular 1.2.0-rc.2 is placed at http://plnkr.co/edit/iaC2NY
- Older version with Angular 1.1.5 is placed at http://plnkr.co/mVR4P4

## Getting started

Optionally: to install with bower, use:
```
bower install --save angularjs-toaster
```
or with npm :
```
npm install --save angularjs-toaster
```
* Link scripts:

```html
<link href="https://cdnjs.cloudflare.com/ajax/libs/angularjs-toaster/0.4.9/toaster.min.css" rel="stylesheet" />
<script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.2.0/angular.min.js" ></script>
<script src="https://code.angularjs.org/1.2.0/angular-animate.min.js" ></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/angularjs-toaster/0.4.9/toaster.min.js"></script>
```

* Add toaster container directive: 

```html
<toaster-container></toaster-container>
```

* Prepare the call of toaster method:

```js
// Display an info toast with no title
angular.module('main', ['toaster', 'ngAnimate'])
	.controller('myController', function($scope, toaster) {
	    $scope.pop = function(){
	        toaster.pop('success', "title", "text");
	    };
	});
```

* Call controller method on button click:

```html
<div ng-controller="myController">
    <button ng-click="pop()">Show a Toaster</button>
</div>
```

### Close Button

The Close Button's visibility can be configured at three different levels:

* Globally in the config for all toast types:
```html
<toaster-container toaster-options="'close-button': true"></toaster-container>
```

* Per info-class type:
By passing the close-button configuration as an object instead of a boolean, you can specify the global behavior an info-class type should have.
```html
<toaster-container toaster-options="
    {'close-button':{ 'toast-warning': true, 'toast-error': false } }">
</toaster-container>
```
If a type is not defined and specified, the default behavior for that type is false.

* Per toast constructed via toaster.pop('success', "title", "text"):
```html
toaster.pop({
                type: 'error',
                title: 'Title text',
                body: 'Body text',
                showCloseButton: true
            });
```
This option is given the most weight and will override the global configurations for that toast.  However, it will not persist to other toasts of that type and does not alter or pollute the global configuration.

### Body Output Type
The rendering of the body content is configurable at both the Global level, which applies to all toasts, and the individual toast level when passed as an argument to the toast.

There are three types of body renderings: trustedHtml', 'template', 'templateWithData'.

 - trustedHtml:  When using this configuration, the toast will parse the body content using 
	`$sce.trustAsHtml(toast.body)`.
	If the html can be successfully parsed, it will be bound to the toast via `ng-bind-html`.  If it cannot be parsed as "trustable" html, an exception will be thrown.	

 - template:  Will use the `toast.body` if passed as an argument, else it will fallback to the template bound to the `'body-template': 'toasterBodyTmpl.html'` configuration option.
 
 - templateWithData: 
	 - Will use the `toast.body` if passed as an argument, else it will fallback to the template bound to the `'body-template': 'toasterBodyTmpl.html'` configuration option.
	 - Assigns any data associated with the template to the toast.

All three options can be configured either globally for all toasts or individually per toast.pop() call.  If the `body-output-type` option is configured on the toast, it will take precedence over the global configuration for that toast instance.

 - Globally:
```html
<toaster-container toaster-options="'body-output-type': 'template'"></toaster-container>
```
 - Per toast:
  
```js
toaster.pop({
            type: 'error',
            title: 'Title text',
            body: 'Body text',
            bodyOutputType: 'trustedHtml'
});
```

### On Hide Callback
A callback function can be attached to each toast instance.  The callback will be invoked upon toast removal.  This can be used to chain toast calls.

```js
toaster.pop({
            title: 'A toast',
		    body: 'with a callback',
			onHideCallback: function () { 
			    toaster.pop({
			        title: 'A toast',
				    body: 'invoked as a callback'
				});
			}
});
```


### Other Options

```html
// Change display position
<toaster-container toaster-options="{'position-class': 'toast-top-full-width'}"></toaster-container>
```

### Animations
Unlike toastr, this library relies on ngAnimate and CSS3 transformations for optional animations.  To include and use animations, add a reference to angular-animate.min.js (as described in Getting started - Link scripts) and add ngAnimate as a dependency alongside toaster. 

```js
// Inject ngAnimate to enable animations
angular.module('main', ['toaster', 'ngAnimate']);
```
If you do not want to use animations, you can safely remove the angular-animate.min.js reference as well as the injection of ngAnimate.  Toasts will be displayed without animations.

		
## Author
**Jiri Kavulak**

## Credits
Inspired by http://codeseven.github.io/toastr/demo.html.

## Copyright
Copyright © 2013 [Jiri Kavulak](https://twitter.com/jirikavi).

## License 
AngularJS-Toaster is under MIT license - http://www.opensource.org/licenses/mit-license.php

##Changes Log
## v0.4.13
- Add option in function toaster.pop() , `toastId` to define 'uid', use the function 'toaster.clear ()'

```js
var _toaster = {
                    type:      null,
                    title:     null,
                    body:      null,
                    timeout:   null,
                    toasterId: 'CategoryMenu',
                    toastId:   'CategoryMenuAlert'
                }
```

- Add option in function toaster.clear()
 * toaster.clear(); --> clearAll with ToasterId = undefined;
 * toaster.clear('*'); -> ClearAll()
 * toaster.clear('clearID'); -> clearAll() with toaster have ToasterId = 'clearID'
* toaster.clear('clearID', 'toastID'); -> Just clearAll with toasts have uid = 'toastID' in  ToasterId = 'clearID'.