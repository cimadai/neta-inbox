/// <reference path="./third_party/jquery/jquery.d.ts" />
/// <reference path="./third_party/auth0/auth0.lock.d.ts" />
/// <reference path="./alice.common.ts" />
/// <reference path="./alice.api_facade.ts" />
interface Window {
    AUTH0_CLIENT_ID: string;
    AUTH0_DOMAIN: string;
    AUTH0_CALLBACK_URL: string;
}

import A_ = Alice;
$(function () {
    var Common = A_.Common;
    var Api = A_.ApiFacade;

    if (window.AUTH0_CLIENT_ID) {
        var lock = new Auth0Lock(window.AUTH0_CLIENT_ID, window.AUTH0_DOMAIN);

        console.log("callback = " + window.AUTH0_CALLBACK_URL);
        $(".btn-login").click(function (e) {
            e.preventDefault();
            lock.show({
                callbackURL: window.AUTH0_CALLBACK_URL,
                connections: ["google-oauth2"],
                responseType: 'code',
            }, undefined);
        });
    }

    console.log(Common.i18n("event.not.assignment"));
    $(".event-reaction").click(function (e) {
        e.preventDefault();
        var $btn = $(this);
        Api.Event.toggleEventReaction($btn.data("eventId"), $btn.data("reactionTypeId"), function (json) {
            console.log(json);
        });

    })
});
