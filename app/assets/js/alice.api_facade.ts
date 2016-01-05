/// <reference path="./third_party/jquery/jquery.d.ts" />
/// <reference path="./alice.common.ts" />
/// <reference path="./alice.notify.ts" />

namespace Alice.ApiFacade {
    var baseUrl = Alice.Common.baseUrl;
    var apiBaseUrl = Alice.Common.baseUrl + "api/";

    export var Event = {
        toggleEventReaction: function (eventId: number, reactionTypeId: number, onSuccess?, onError?): void {
            var url = apiBaseUrl + "event/" + eventId + "/" + reactionTypeId;
            var data = {};
            ajaxPost(url, data, onSuccess, onError);
        },
        searchEvent: function (query: string): void {
            var url = baseUrl + "event/list/search?q=" + query;
            redirect(url);
        }
    };
    export var EventTag = {
        getAllTags: function (onSuccess?, onError?): void {
            var url = apiBaseUrl + "event/tags";
            ajaxGet(url, onSuccess, onError)
        },
        addTag: function (key: string, onSuccess?, onError?): void {
            var url = apiBaseUrl + "event/tag/new/" + key;
            var data = {};
            ajaxPost(url, data, onSuccess, onError);
        }
    };

    var ajaxDefault = {
        data: {format: "json"},
        dataType: "json",
        cache : false
    };

    var ajaxBase = function (options) {
        var opt = $.extend({}, ajaxDefault, options);
        $.ajax(opt);
    };

    var createSuccessProxy = function (callback) {
        return function (data, status, xhr) {
            var redirectUrl = xhr.getResponseHeader("X-AJAX-REDIRECT");
            if (redirectUrl) {
                redirect(redirectUrl);
            }
            var pushGrowl = true;
            if (callback) {
                var ret = callback(data);
                if (ret === false) {
                    pushGrowl = false;
                }
            }
            if (data) {
                if (data.result !== 0 && pushGrowl) {
                    var errorCode = data.domain && data.code ? "Error(" + data.domain + "-" + data.code + ") : " : "";
                    var key = (data.messageKey || data.caption);
                    var args = $.map(data.args || [], function (arg, idx) { return Alice.Common.Utils.escapeHTML(arg); });
                    var keyAndArgs = data.args ? [key].concat(args) : [key];
                    Alice.Notify.error(errorCode + Alice.Common.i18n.apply(this, keyAndArgs));
                }
            }
        };
    };
    var createErrorProxy = function (callback) {
        return function (data) {
            var doDefaultHandling = true;
            if (callback) {
                doDefaultHandling = !(callback(data) === false);
            }

            if (doDefaultHandling) {
                Alice.Notify.error(Alice.Common.i18n("error.cannot.execute.api"));
            }
        }
    };
    var ajaxGet = function (url, onSuccess, onError) {
        ajaxBase({
            type: "GET",
            url: url,
            success: createSuccessProxy(onSuccess),
            error: createErrorProxy(onError)
        });
    };
    var ajaxPost = function (url, data, onSuccess, onError) {
        ajaxBase({
            type: "POST",
            url: url,
            data: data,
            success: createSuccessProxy(onSuccess),
            error: createErrorProxy(onError)
        });
    };
    var ajaxDelete = function (url, data, onSuccess, onError) {
        ajaxBase({
            type: "DELETE",
            url: url,
            data: data,
            success: createSuccessProxy(onSuccess),
            error: createErrorProxy(onError)
        });
    };


    export function redirect(url) {
        location.href = url;
    }

}
