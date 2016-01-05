/// <reference path="./third_party/jquery/jquery.d.ts" />

interface Window {
    i18n(val): string;
}

namespace Alice.Common {
    export function i18n(...args:any[]): string {
        return window.i18n.apply(this, args);
    }
    export var baseUrl = $("base").attr("href");

    export var Utils = {
        is: function (type, obj) {
            var clas = Object.prototype.toString.call(obj).slice(8, -1);
            return obj !== undefined && obj !== null && clas === type;
        },
        None : 0.0,
        K : 1024.0,
        M : 1024.0 * 1024.0,
        G : 1024.0 * 1024.0 * 1024.0,
        T : 1024.0 * 1024.0 * 1024.0 * 1024.0,
        P : 1024.0 * 1024.0 * 1024.0 * 1024.0 * 1024.0,
        formatBps : function (bps) {
            var value = bps;
            var unit = "bps";
            if (Utils.G <= bps) {
                value = bps / Utils.G;
                unit = "Gbps";
            } else if (Utils.M <= bps) {
                value = bps / Utils.M;
                unit = "Mbps";
            } else if (Utils.K <= bps) {
                value = bps / Utils.K;
                unit = "Kbps";
            }
            // TODO: Fixme
            //return Globalize.format(value, "n2") + " " + unit;
        },
        formatBytesByOrder: function (order, bytes, useShortUnit) {
            useShortUnit = (useShortUnit === true);
            var value = bytes;
            var unit = useShortUnit ? "B" : "Bytes";
            switch (order) {
                case Utils.P:
                    value = bytes / Utils.P;
                    unit = useShortUnit ? "PB" : "PiBytes";
                    break;
                case Utils.T:
                    value = bytes / Utils.T;
                    unit = useShortUnit ? "TB" : "TiBytes";
                    break;
                case Utils.G:
                    value = bytes / Utils.G;
                    unit = useShortUnit ? "GB" : "GiBytes";
                    break;
                case Utils.M:
                    value = bytes / Utils.M;
                    unit = useShortUnit ? "MB" : "MiBytes";
                    break;
                case Utils.K:
                    value = bytes / Utils.K;
                    unit = useShortUnit ? "KB" : "KiBytes";
                    break;
            }
            // TODO: Fixme
            // return Globalize.format(value, "n2") + " " + unit;
        },
        getUnitFromByte: function(bytes) {
            if (Utils.P <= bytes) return Utils.P;
            else if (Utils.T <= bytes) return Utils.T;
            else if (Utils.G <= bytes) return Utils.G;
            else if (Utils.M <= bytes) return Utils.M;
            else if (Utils.K <= bytes) return Utils.K;
            else return Utils.None;
        },
        formatBytes: function (bytes, useShortUnit) {
            return Utils.formatBytesByOrder(Utils.getUnitFromByte(bytes), bytes, useShortUnit)
        },
        formatIn2: function(num) {
            if (num < 10) {
                return '0' + num;
            } else {
                return num;
            }
        },
        formatSeconds: function(seconds) {
            var parsed = this.parseSeconds(seconds);
            return Utils.formatIn2(parsed.hours) + ':' + Utils.formatIn2(parsed.minutes) + ':' + Utils.formatIn2(parsed.seconds);
        },
        parseSeconds: function(seconds) {
            // TODO: days
            var hours = Math.floor(seconds / 3600);
            var rem = seconds;
            if (0 < hours) {
                rem = seconds % (3600 * hours);
            }
            var minutes = Math.floor(rem / 60);
            if (0 < minutes) {
                rem = rem % (60 * minutes);
            }
            return {hours: hours, minutes: minutes, seconds: rem};
        },
        isFunction: function (value) {
            return typeof(value) === 'function';
        },
        isTrue: function (value) {
            return value === true;
        },
        isNullOrEmpty: function (value) {
            if (value === null || value === undefined) {
                return true;
            } else if (typeof value === "string") {
                return value === "";
            } else if ($.isArray(value)) {
                return value.length === 0;
            } else {
                var key;
                for ( key in value ) {
                    if (value.hasOwnProperty(key)) {
                        return false;
                    }
                }
                return true;
            }
        },
        createPoOpt: function(placement, title, content, trigger) {
            return {placement: placement, title : title, content : content, animation : false, trigger : trigger};
        },
        setDisabledPushedBtn: function($btn) {
            // 一度押したsubmitボタンはdisabled状態にして、loadingアイコンを出す
            $btn.addClass("disabled");
            $btn.find("i").removeClass("icon-ok");
            $btn.prepend($("<img>").attr("src", "../../../public/images/loading.gif"));
        },
        isBtnHasDisabled: function ($btn) {
            return $btn.is(".disabled");
        },
        getHashFragmentOfURL: function () {
            var rawHashOfURL = location.href.split('#')[1];
            return rawHashOfURL ? decodeURI(rawHashOfURL) : undefined;
        },
        resetHashFragment: function (hashFragment) {
            if (typeof hashFragment === "object") {
                var hashAry = Object.keys(hashFragment).map(function(key) { return key + "=" + hashFragment[key]; });
                location.hash = hashAry.join("&");
            } else {
                // 渡された文字列でハッシュを置き換える
                location.hash = hashFragment;
            }
        },
        saveHashFragmentToCookie: function (cookieName) {
            var hashParams = Utils.parseQueryFormattedString(Utils.getHashFragmentOfURL());
            // TODO: Fixme
            //$.cookie(cookieName, JSON.stringify(hashParams), {path: "/"});
            return hashParams;
        },
        loadHashFragment: function (cookieName) {
            var hashFromURL = Utils.loadHashFragmentFromURL();
            return hashFromURL ? hashFromURL : Utils.loadHashFragmentFromCookie(cookieName);
        },
        loadHashFragmentFromURL: function () {
            var hashOfURL = Utils.getHashFragmentOfURL();
            return hashOfURL ? Utils.parseQueryFormattedString(hashOfURL) : undefined;
        },
        loadHashFragmentFromCookie: function (cookieName) {
            try {
                // TODO: Fixme
                //return JSON.parse($.cookie(cookieName));
            } catch (e) {
                return undefined;
            }
        },
        parseQueryFormattedString: function(url) {
            var params = (url && url.lastIndexOf("?") > 0) ? url.split("?")[1] : url;

            if (typeof params === "undefined") {
                return params;
            }

            if (params.match("#")) {
                params = params.split("#")[0];
            }

            var result = {};
            $.each(params.split("&"), function() {
                var value = this.split("=");
                result[value[0]] = value[1];
            });
            return result;
        },
        getActualItemOfContextMenu: function(e){
            var $item = $(e.target);
            if ($item.prop("tagName") == "I") {
                // icon(I)をクリックした場合は親要素のAnchorを取得
                return $item.parent();
            } else {
                return $item;
            }
        },
        escapeHTML: function(val) {
            return $('<div />').text(val).html();
        }
    };
}
