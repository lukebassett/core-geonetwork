(function() {
  goog.provide('gn_metadata_manager_service');

  var module = angular.module('gn_metadata_manager_service', []);

  module.provider('gnMetadataManagerService',
      function() {
        this.$get = [
          '$q',
          '$rootScope',
          '$http',
          '$translate',
          '$compile',
          function($q, $rootScope, $http, $translate, $compile) {
            var _select = function(uuid, andClearSelection, action) {
              var defer = $q.defer();
              $http.get(
                  'metadata.select@json?' + (uuid ? 'id=' + uuid : '') +
                  (andClearSelection ? '' : '&selected=' + action))
                    .success(function(data, status) {
                    defer.resolve(data);
                  }).error(function(data, status) {
                    defer.reject(error);
                  });
              return defer.promise;
            };

            return {
              // TODO : move select to SearchManagerService
              select: function(uuid, andClearSelection) {
                return _select(uuid, andClearSelection, 'add');
              },
              unselect: function(uuid) {
                return _select(uuid, false, 'remove');
              },
              selectAll: function() {
                return _select(null, false, 'add-all');
              },
              selectNone: function() {
                return _select(null, false, 'remove-all');
              },
              view: function(md) {
                window.open('../../?uuid=' + md['geonet:info'].uuid,
                    'gn-view');
              },
              edit: function(md) {
                location.href = 'catalog.edit?#/metadata/' +
                    md['geonet:info'].id;
              }
            };
          }];
      });
})();