// Compiled by ClojureScript 0.0-2505
goog.provide('index');
goog.require('cljs.core');
goog.require('goog.dom');
goog.require('goog.events.EventType');
goog.require('util');
goog.require('landing');
goog.require('goog.events');
goog.require('goog.net.XhrIo');
goog.require('codepair');
goog.require('cljs.reader');
index.onClickHandler = (function onClickHandler(){
var currentUser = "twashing@gmail.com";
var navigatorId = navigator.id;
util.console_log.call(null,"Signin CLICKED");

navigatorId.watch(cljs.core.clj__GT_js.call(null,new cljs.core.PersistentArrayMap(null, 3, [new cljs.core.Keyword(null,"loggedInUser","loggedInUser",-239133008),currentUser,new cljs.core.Keyword(null,"onlogin","onlogin",-866049072),index.verifyAssertion,new cljs.core.Keyword(null,"onlogout","onlogout",2097570828),codepair.signoutUser], null)));

return navigatorId.request();
});
index.verifyAssertion = (function verifyAssertion(assertion){
util.console_log.call(null,[cljs.core.str("verifyAssertion CALLED / assertion: "),cljs.core.str(assertion)].join(''));

return codepair.edn_xhr.call(null,new cljs.core.PersistentArrayMap(null, 4, [new cljs.core.Keyword(null,"method","method",55703592),new cljs.core.Keyword(null,"post","post",269697687),new cljs.core.Keyword(null,"url","url",276297046),"/verify-assertion",new cljs.core.Keyword(null,"data","data",-232669377),new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"assertion","assertion",-1645134882),assertion], null),new cljs.core.Keyword(null,"on-complete","on-complete",-1531183971),cljs.core.partial.call(null,codepair.basicHandler,(function (e,xhr){
var data = xhr.getResponseText();
var responseF = cljs.reader.read_string.call(null,data);
var groupname = new cljs.core.Keyword(null,"name","name",1843675177).cljs$core$IFn$_invoke$arity$1(cljs.core.first.call(null,new cljs.core.Keyword(null,"groups","groups",-136896102).cljs$core$IFn$_invoke$arity$1(new cljs.core.Keyword(null,"system","system",-29381724).cljs$core$IFn$_invoke$arity$1(cljs.core.first.call(null,new cljs.core.Keyword(null,"uresult","uresult",-615011743).cljs$core$IFn$_invoke$arity$1(responseF))))));
var username = new cljs.core.Keyword(null,"username","username",1605666410).cljs$core$IFn$_invoke$arity$1(cljs.core.first.call(null,new cljs.core.Keyword(null,"users","users",-713552705).cljs$core$IFn$_invoke$arity$1(cljs.core.first.call(null,new cljs.core.Keyword(null,"groups","groups",-136896102).cljs$core$IFn$_invoke$arity$1(new cljs.core.Keyword(null,"system","system",-29381724).cljs$core$IFn$_invoke$arity$1(cljs.core.first.call(null,new cljs.core.Keyword(null,"uresult","uresult",-615011743).cljs$core$IFn$_invoke$arity$1(responseF))))))));
return cljs.core.swap_BANG_.call(null,landing.user_state,((function (data,responseF,groupname,username){
return (function (inp){
return new cljs.core.PersistentArrayMap(null, 3, [new cljs.core.Keyword(null,"groupname","groupname",-493355733),groupname,new cljs.core.Keyword(null,"username","username",1605666410),username,new cljs.core.Keyword(null,"source","source",-433931539),responseF], null);
});})(data,responseF,groupname,username))
);
}))], null));
});
index.start = (function start(){
var temp__4124__auto__ = goog.dom.getElement("signin");
if(cljs.core.truth_(temp__4124__auto__)){
var signinLink = temp__4124__auto__;
return signinLink.onclick = index.onClickHandler;
} else {
return null;
}
});
index.start.call(null);

//# sourceMappingURL=index.js.map