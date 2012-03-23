(function () {
  function forEach(arr, f) {
    for (var i = 0, e = arr.length; i < e; ++i) f(arr[i]);
  }
  
  function arrayContains(arr, item) {
    if (!Array.prototype.indexOf) {
      var i = arr.length;
      while (i--) {
        if (arr[i] === item) {
          return true;
        }
      }
      return false;
    }
    return arr.indexOf(item) != -1;
  } 

  function scriptHint(editor, keywords, getToken) {
    // Find the token at the cursor
    var cur = editor.getCursor(), token = getToken(editor, cur), tprop = token;
    // If it's not a 'word-style' token, ignore the token.
      token = tprop = {start: cur.ch, end: cur.ch, string: "", state: token.state};
    // If it is a property, find out what it is a property of.
    if (!context) var context = [];
    context.push(tprop);

    return {list: getCompletions(token, context, keywords),
            from: {line: cur.line, ch: token.start},
            to: {line: cur.line, ch: token.end}};
  }

  CodeMirror.javascriptHint = function(editor) {
    return scriptHint(editor, javascriptKeywords,
                      function (e, cur) {return e.getTokenAt(cur);});
  }


  var javascriptKeywords = ("<!-- <!DOCTYPE> <a> <abbr> <acronym> <address> <applet> <area> <article> <aside> <audio> <b> <base> <basefont> <bdo> <bdi> <big> <blockquote> <body> <br> <button> <canvas> <caption> <center> <cite> <code> <col> <colgroup> <command> <datalist> <dd> <del> <details> <dfn> <dir> <div> <dl> <dt> <em> <embed> <fieldset> <figcaption> <figure> <font> <footer> <form> <frame> <frameset> <h1> <h2> <h3> <h4> <h5> <h6> <head> <header> <hgroup> <hr> <html> <i> <iframe> <img> <input> <ins> <keygen> <kbd> <label> <legend> <li> <link> <map> <mark> <menu> <meta> <meter> <nav> <noframes> <noscript> <object> <ol> <optgroup> <option> <output> <p> <param> <pre> <progress> <q> <rp> <rt> <ruby> <s> <samp> <script> <section> <select> <small> <source> <span> <strike> <strong> <style> <sub> <summary> <sup> <table> <tbody> <td> <textarea> <tfoot> <th> <thead> <time> <title> <tr> <track> <tt> <uul> <var> <video> <wbr> <xmp>").split(" ");

  function getCompletions(token, context, keywords) {
    var found = [], start = token.string;
    function maybeAdd(str) {
    if (str.indexOf(start) == 0 && !arrayContains(found, str)) found.push(str);

    }
      // If not, just look in the window object and any local scope
      // (reading into JS mode internals to get at the local variables)
    forEach(keywords, maybeAdd);
    return found;
  }
})();
