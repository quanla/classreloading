"use strict";

(function() {

    angular.module('classreloading.hello', [
        ])

        .config(function ($stateProvider) {

            $stateProvider
                .state('hello', {
                    url: '/hello',
                    templateUrl: "/hello/Hello.jade?v=" + App.version,
                    controller: "classreloading.hello.Ctrl"
                })
            ;
        })

        .controller("classreloading.hello.Ctrl", function($scope, $modal, ContactService) {
            ContactService.getAll().success(function(contacts) {
                $scope.contacts = contacts;
            });

            $scope.showAddForm = function() {
                $modal.open({
                    templateUrl: "/hello/AddContactModal.jade",
                    controller: "classreloading.hello.AddContactModalCtrl"
                })
                    .result.then(function(contact) {
                        $scope.contacts.push(contact);
                    });
            };

            $scope.remove = function(contact) {
                if (!confirm("Remove this contact: " + contact.name + " - " + contact.phone + "?")) {
                    return;
                }

                ContactService.remove(contact).success(function() {
                    $scope.contacts.splice($scope.contacts.indexOf(contact), 1);
                });
            };

        })

        .controller("classreloading.hello.AddContactModalCtrl", function($scope, ContactService, $modalInstance) {
            $scope.contact = {};
            $scope.add = function() {
                ContactService.add($scope.contact).success(function(contact1) {
                    $modalInstance.close(contact1);
                });
            };
        })


        .factory('ContactService', function ($http) {
            return {
                getAll : function() {
                    return $http.post("/contact?action=getAll", 0);
                },
                add : function(contact) {
                    return $http.post("/contact?action=add", contact);
                },
                remove : function(contact) {
                    return $http.post("/contact?action=remove&id=" + contact.id, contact);
                }
            };
        })
    ;

})();