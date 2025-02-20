/*global RequisitionNode:true */

/**
* @author Alejandro Galue <agalue@opennms.org>
* @copyright 2014 The OpenNMS Group, Inc.
*/

'use strict';

describe('Controller: InterfaceController', function () {

  var scope, $q, controllerFactory, mockModalInstance, mockRequisitionsService = {};

  var foreignSource = 'test-requisition';
  var foreignId = '1001';
  var node = new RequisitionNode(foreignSource, { 'foreign-id': foreignId });
  var services = ['ICMP', 'SNMP', 'HTTP'];
  node.addNewInterface();
  node.interfaces[0].ipAddress = '10.0.0.1';

  function createController() {
    return controllerFactory('InterfaceController', {
      $scope: scope,
      $uibModalInstance: mockModalInstance,
      RequisitionsService: mockRequisitionsService,
      foreignSource: foreignSource,
      foreignId: foreignId,
      requisitionInterface: node.interfaces[0],
      ipBlackList: []
    });
  }

  beforeEach(module('onms-requisitions', function($provide) {
    $provide.value('$log', console);
  }));

  beforeEach(inject(function($rootScope, $controller, _$q_) {
    scope = $rootScope.$new();
    controllerFactory = $controller;
    $q = _$q_;
  }));

  beforeEach(function() {
    mockRequisitionsService.getAvailableServices = jasmine.createSpy('getAvailableServices');
    var servicesDefer = $q.defer();
    servicesDefer.resolve(services);
    mockRequisitionsService.getAvailableServices.andReturn(servicesDefer.promise);

    mockModalInstance = {
      close: function(obj) { console.info(obj); },
      dismiss: function(msg) { console.info(msg); }
    };
  });

  it('test controller', function() {
    createController();
    scope.$digest();
    expect(scope.requisitionInterface.ipAddress).toBe(node.interfaces[0].ipAddress);
    expect(scope.snmpPrimaryFields[0].title).toBe('Primary');
    scope.addService();
    expect(scope.requisitionInterface.services.length).toBe(1);
    scope.removeService(0);
    expect(scope.requisitionInterface.services.length).toBe(0);
    expect(scope.availableServices.length).toBe(3);
    expect(scope.availableServices[0]).toBe('ICMP');
  });

});
