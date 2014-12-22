"use strict";

(function() {

    angular.module('classreloading.hello', [
        ])

        .config(function ($stateProvider) {

            $stateProvider
                .state('hello', {
                    url: '/hello',
                    templateUrl: "/hello/Hello.jade?v=" + App.version,
                    controller: function($scope, PersonService) {
                        PersonService.getAll().success(function(persons) {
                            $scope.persons = persons;
                        });
                    }
                })
            ;
        })

        .factory('PersonService', function ($http) {
            return {
                getAll : function() {
                    return $http.post("/person", 0);
                }
            };
        })
    ;

})();