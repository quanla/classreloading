"use strict";

(function() {
    /* App Module */
    angular.module("classreloading.app", [
            'ui.router',
            'ui.bootstrap',
            'classreloading.hello'
        ])

        .run(function ($rootScope, $state, $stateParams) {
            // It's very handy to add references to $state and $stateParams to the $rootScope
            // so that you can access them from any scope within your applications.For example,
            // <li ng-class="{ active: $state.includes('contacts.list') }"> will set the <li>
            // to active whenever 'contacts.list' or one of its decendents is active.
            $rootScope.$state = $state;
            $rootScope.$stateParams = $stateParams;
            
            $rootScope.$on('$stateChangeSuccess', function(event, toState, toParams, fromState, fromParams){ 
                window.scrollTo(0, 0);
            })
        })

        .config(function ($stateProvider, $urlRouterProvider) {
            $urlRouterProvider
                // If the url is ever invalid, e.g. '/asdf', then redirect to '/' aka the home state
                .otherwise("/hello");
        })
        
        .config(function ($locationProvider) {
            $locationProvider.html5Mode(false).hashPrefix("!");
        })
        
    ;
})();
