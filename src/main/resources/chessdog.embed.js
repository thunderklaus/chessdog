chessdog={};
window.chessdog.Board=function(a){a.innerHTML="<iframe width='600' height='385' frameborder='0' style='border: none;'></iframe>";this.frame=a.childNodes[0];this.wnd=this.frame.contentWindow;this.doc=this.wnd.document;var b=this.wnd,c=this.doc;c.open();c.write("<!doctype html>\n<html><head></head><body><div id='removeme' style='font-family: Arial Unicode MS,Arial,sans-serif;font-size: small;'>Loading chess board...</div></body></html>\n");c.close();setTimeout(function(){var a=a||[];a.push(["_setAccount","UA-5412976-12"]);
a.push(["_setDomainName","none"]);a.push(["_setAllowLinker",!0]);a.push(["_trackPageview"]);b._gaq=a;(function(){var a=c.createElement("script");a.type="text/javascript";a.async=!0;a.src=("https:"==c.location.protocol?"https://ssl":"http://www")+".google-analytics.com/ga.js";c.getElementsByTagName("head")[0].appendChild(a)})();a=c.createElement("script");a.type="text/javascript";a.src="http://${Host}/jackembed/jackembed.nocache.js";c.getElementsByTagName("head")[0].appendChild(a)},0)};
chessdog.Board.prototype.reset=function(){if(assertChessdogReady(this))return this.wnd.reset()};chessdog.Board.prototype.getFen=function(){if(assertChessdogReady(this))return this.wnd.getFen()};chessdog.Board.prototype.setFen=function(a){var b=this;onChessdogReady(this,function(){b.wnd.setFen(a)})};chessdog.Board.prototype.getPgn=function(){if(assertChessdogReady(this))return this.wnd.getPgn()};chessdog.Board.prototype.setPgn=function(a){var b=this;onChessdogReady(this,function(){b.wnd.setPgn(a)})};
chessdog.Board.prototype.onReady=function(a){onChessdogReady(this,a)};var onChessdogReady=function(a,b){a.wnd.chessdogReady?b():setTimeout(function(){onChessdogReady(a,b)},100)},assertChessdogReady=function(a){return a.wnd.chessdogReady?!0:(alert("chessdog not ready yet. Have a look at board.onReady()."),!1)};