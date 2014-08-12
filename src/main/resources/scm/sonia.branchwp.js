/*
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */

Ext.ns('Sonia.branchwp');

Sonia.branchwp.ConfigPanel = Ext.extend(Sonia.repository.PropertiesFormPanel, {
  
  formTitleText: 'Branch Write Protection',
  enabledText: 'Enable',
  colBranchText: 'Branch',
  colNameText: 'Name',
  colGroupText: 'Is Group',
  colDenyText: 'Deny',
  addText: 'Add',
  removeTest: 'Remove',
  
  addIcon: 'resources/images/add.gif',
  removeIcon: 'resources/images/delete.gif',
  helpIcon: 'resources/images/help.gif',
  
  enableHelpText: 'Enable Branch write protection. \n\
    Only admins, owners and users defined in the whitelist below are able to write.',
  branchwpGridHelpText: 'Branch write protection whitelist. Deny comes always for allow permissions. \n\
                         <b>Note:</b> You can use glob syntax and the placeholders {username} and {mail} for branch names.',
  
  branchwpStore: null,
  
  initComponent: function(){
    this.branchwpStore = new Ext.data.ArrayStore({
      root: 'permissions',
      fields: [
        {name: 'branch'},
        {name: 'name'},
        {name: 'group', type: 'boolean'},
        {name: 'deny', type: 'boolean'}
      ],
      sortInfo: {
        field: 'branch'
      }
    });
    
    this.loadBranchwpPermissions(this.branchwpStore, this.item);
  
    var selectionModel = new Ext.grid.RowSelectionModel({
      singleSelect: true
    });
  
    var branchwpColModel = new Ext.grid.ColumnModel({
      defaults: {
        sortable: true,
        editable: true
      },
      columns: [{
        id: 'branch',
        dataIndex: 'branch',
        header: this.colBranchText,
        editor: Ext.form.TextField
      },{
        id: 'name',
        dataIndex: 'name',
        header: this.colNameText,
        editor: Ext.form.TextField
      },{
        id: 'group',
        dataIndex: 'group',
        xtype: 'checkcolumn',
        header: this.colGroupText,
        width: 40,
        editable: true
      },{
        id: 'deny',
        dataIndex: 'deny',
        xtype: 'checkcolumn',
        header: this.colDenyText,
        width: 40,
        editable: true
      }]
    });

    var config = {
      title: this.formTitleText,
      items: [{
        xtype: 'checkbox',
        fieldLabel: this.enabledText,
        name: 'branchwpEnabled',
        inputValue: 'true',
        property: 'branchwp.enabled',
        helpText: this.enableHelpText
      },{
        id: 'branchwpGrid',
        xtype: 'editorgrid',
        clicksToEdit: 1,
        autoExpandColumn: 'branch',
        frame: true,
        width: '100%',
        autoHeight: true,
        autoScroll: false,
        colModel: branchwpColModel,
        sm: selectionModel,
        store: this.branchwpStore,
        viewConfig: {
          forceFit:true
        },
        tbar: [{
          text: this.addText,
          scope: this,
          icon: this.addIcon,
          handler : function(){
            var Permission = this.branchwpStore.recordType;
            var p = new Permission();
            var grid = Ext.getCmp('branchwpGrid');
            grid.stopEditing();
            this.branchwpStore.insert(0, p);
            grid.startEditing(0, 0);
          }
        },{
          text: this.removeText,
          scope: this,
          icon: this.removeIcon,
          handler: function(){
            var grid = Ext.getCmp('branchwpGrid');
            var selected = grid.getSelectionModel().getSelected();
            if ( selected ){
              this.branchwpStore.remove(selected);
            }
          }
        }, '->',{
          id: 'branchwpGridHelp',
          xtype: 'box',
          autoEl: {
            tag: 'img',
            src: this.helpIcon
          }
        }]
      }]
    };
    
    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.branchwp.ConfigPanel.superclass.initComponent.apply(this, arguments);
  },
  
  afterRender: function(){
    // call super
    Sonia.branchwp.ConfigPanel.superclass.afterRender.apply(this, arguments);

    Ext.QuickTips.register({
      target: Ext.getCmp('branchwpGridHelp'),
      title: '',
      text: this.branchwpGridHelpText,
      enabled: true
    });
  },
  
  loadBranchwpPermissions: function(store, repository){
    if (debug){
      console.debug('load branchwp properties');
    }
    if (!repository.properties){
      repository.properties = [];
    }
    Ext.each(repository.properties, function(prop){
      if ( prop.key === 'branchwp.permissions' ){
        var value = prop.value;
        this.parsePermissions(store, value);
      }
    }, this);
  },
  
  parsePermissions: function(store, permString){
    var parts = permString.match(/[^,]+,[^,]+;/g);
    if (debug){
      console.debug('branch permissions:');
      console.debug( parts );
    }
    Ext.each(parts, function(part){
      if ( part.endsWith(';') ){
        part = part.substring(0, part.length -1);
      }
      var pa = part.split(',');
      var deny = false;
      if (pa[0].indexOf('!') === 0){
        deny = true;
        pa[0] = pa[0].substring(1);
      }
      var group = false;
      if ( pa[1].indexOf('@') === 0 ){
        group = true;
        pa[1] = pa[1].substring(1);
      }
      var Permission = store.recordType;
      var p = new Permission({
        branch: pa[0],
        group: group,
        name: pa[1],
        deny: deny
      });
      if (debug){
        console.debug( 'add permission: ' );
        console.debug( p );
      }
      store.add(p);
    });
  },
  
  storeExtraProperties: function(repository){
    if (debug){
      console.debug('store branchpw properties');
    }
    
    // delete old permissions
    Ext.each(repository.properties, function(prop, index){
      if ( prop.key === 'branchwp.permissions' ){
        delete repository.properties[index];
      }
    });
    
    var permissionString = '';
    this.branchwpStore.data.each(function(r){
      var p = r.data;
      if (p.deny){
        permissionString += '!';
      }
      permissionString += p.branch + ',';
      if (p.group){
        permissionString += '@';
      }
      permissionString += p.name + ';';
    });
    
    if (debug){
      console.debug('add branchwp permission string: ' + permissionString);
    }
    
    repository.properties.push({
      key: 'branchwp.permissions',
      value: permissionString
    });
  }
  
});

// register xtype
Ext.reg("branchwpConfigPanel", Sonia.branchwp.ConfigPanel);

// register panel
Sonia.repository.openListeners.push(function(repository, panels){
  if (Sonia.repository.isOwner(repository)){
    var type = Sonia.repository.getTypeByName( repository.type );
    if ( type && type.supportedCommands && type.supportedCommands.indexOf('BRANCHES') >= 0){
      panels.push({
        xtype: 'branchwpConfigPanel',
        item: repository
      });
    }
  }
});