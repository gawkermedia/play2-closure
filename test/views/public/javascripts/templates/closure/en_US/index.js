// This file was automatically generated from index.soy.
// Please don't edit this file by hand.

if (typeof closuretest == 'undefined') { var closuretest = {}; }


closuretest.index = function(opt_data, opt_sb) {
  var output = opt_sb || new soy.StringBuilder();
  output.append('Hello world!');
  return opt_sb ? '' : output.toString();
};


closuretest.list = function(opt_data, opt_sb) {
  var output = opt_sb || new soy.StringBuilder();
  output.append(soy.$$escapeHtml(opt_data.name), ': ');
  var itemList7 = opt_data.list;
  var itemListLen7 = itemList7.length;
  for (var itemIndex7 = 0; itemIndex7 < itemListLen7; itemIndex7++) {
    var itemData7 = itemList7[itemIndex7];
    output.append(soy.$$escapeHtml(itemData7), (! (itemIndex7 == itemListLen7 - 1)) ? ', ' : '');
  }
  return opt_sb ? '' : output.toString();
};


closuretest.listInList = function(opt_data, opt_sb) {
  var output = opt_sb || new soy.StringBuilder();
  var itemList14 = opt_data.list;
  var itemListLen14 = itemList14.length;
  for (var itemIndex14 = 0; itemIndex14 < itemListLen14; itemIndex14++) {
    var itemData14 = itemList14[itemIndex14];
    closuretest.list({name: opt_data.name, list: itemData14}, output);
  }
  return opt_sb ? '' : output.toString();
};
