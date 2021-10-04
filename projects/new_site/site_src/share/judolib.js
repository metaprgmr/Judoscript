function info(name, e) {
  var r = '\n';
  var numLoops = 0;
  var theThing = e;
  var msg = name + r;
  for (var i in theThing) {
    msg += i + ' =  ' + eval('theThing.' + i) + r;
    numLoops++;
  }
  return msg + r + 'Number of Properties= ' + numLoops;
}

function parseQueryString (str) {
  str = str ? str : location.search;
  var query = str.charAt(0) == '?' ? str.substring(1) : str;
  query = query.replace(/%26/g, '&');
  var args = new Object();
  if (query) {
    var fields = query.split('&');
    for (var f = 0; f < fields.length; f++) {
      var field = fields[f].split('=');
      args[unescape(field[0].replace(/\+/g, ' '))] = unescape(field[1].replace(/\+/g, ' '));
    }
  }
  return args;
}

function showPanes(leftUrl, rightUrl) {
  if (getTop(this).frames == null) {
    this.location = (leftUrl!=null) ? leftUrl : rightUrl;
    return true;
  }
  var leftPane = getTop(this).frames[1];
  if (leftPane!=null && leftUrl!=null) leftPane.location = leftUrl;
  var rightPane = getTop(this).frames[2];
  if (rightPane!=null && rightUrl!=null) rightPane.location = rightUrl;
}

function showAllPanes(baseUrl, leftUrl, rightUrl) {
  var c = 0;
  if (leftUrl != null)  c += 1;
  if (rirhgUrl != null) c += 2;
  switch(c) {
  case 0:  getTop(this).location = baseUrl; break;
  case 1:  getTop(this).location = baseUrl + '?l=' + leftUrl; break;
  case 2:  getTop(this).location = baseUrl + '?r=' + rightUrl; break;
  default: getTop(this).location = baseUrl + '?l=' + leftUrl + '&r=' + rightUrl; break;
  }
}

function go_to(url) {
  getTop(this).location = url;
}

function getTop(obj) {
  var o = obj;
  while (o != o.parent) o = o.parent;
  return o;
}

function showSyn()    { showPanes('_syn.html', 'syn.html'); }
function showSynT()   { showPanes('_synt.html', 'synt.html'); }
function showSynKW()  { showPanes('_synk.html', 'synk.html'); }
function showSynNT(nt){ showPanes('_synn.html#synn_'+nt, 'synn.html#'+nt); }
function showNT(nt)   { showPanes('_synn.html#synn_'+nt, 'synn_'+nt+'.html'); }
function showObj(obj) { if (obj!='') showPanes('_obj.html#obj_'+obj, 'obj_'+obj+'.html');
                        else showPanes('_obj.html','obj.html');
                      }
function showFA(fa)   { if (fa!='') showPanes('_fa_'+fa+'.html#fa_'+fa, 'fa_'+fa+'.html');
                        else showPanes('_fa.html','fa.html');
                      }
function showSF(sf)   { if (sf!='') showPanes('_sfn.html#sfn_'+sf, 'sfn.html#sfn_'+sf);
                        else showPanes('_sfn.html','sfn.html');
                      }

