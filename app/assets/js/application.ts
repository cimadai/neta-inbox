/// <reference path="./third_party/jquery/jquery.d.ts" />
/// <reference path="./third_party/auth0/auth0.lock.d.ts" />
/// <reference path="./third_party/bootstrap/typeahead.d.ts" />
/// <reference path="./third_party/bootstrap/bootstrap.v3.datetimepicker.d.ts" />
/// <reference path="./alice.common.ts" />
/// <reference path="./alice.api_facade.ts" />
/// <reference path="./alice.modal.ts" />

interface Window {
    AUTH0_CLIENT_ID: string;
    AUTH0_DOMAIN: string;
    AUTH0_CALLBACK_URL: string;
}

import A_ = Alice;
$(function () {
    var Api = A_.Api;
    var Utils = A_.Utils;

    if (window.AUTH0_CLIENT_ID) {
        var lock = new Auth0Lock(window.AUTH0_CLIENT_ID, window.AUTH0_DOMAIN);

        $(".btn-login").click(function (e) {
            e.preventDefault();
            lock.show({
                callbackURL: window.AUTH0_CALLBACK_URL,
                connections: ["google-oauth2"],
                responseType: 'code',
            }, undefined);
        });
    }

    $(".sidebar-search").on("keydown", function (ev) {
        if (ev.keyCode == 13) {
            ev.preventDefault();
            Api.Event.searchEvent($(this).find("input").val());
        }
    }).end().filter(".btn").on("click", function () {
        Api.Event.searchEvent($(this).parent().siblings("input").val());
    });

    function toggleReactionButtonClass($btn) {
        if ($btn.is(".btn-info")) {
            $btn.removeClass("btn-info").addClass("btn-default");
        } else {
            $btn.removeClass("btn-default").addClass("btn-info");
        }
    }
    $(".event-reaction").click(function (ev: BaseJQueryEventObject) {
        ev.preventDefault();
        var $btn = $(this);
        Api.Event.toggleEventReaction($btn.data("eventId"), $btn.data("reactionTypeId"), function (json) {
            toggleReactionButtonClass($btn);
            $btn.find(".badge").text(json.reactions.length);
            var $reactionsArea = $btn.siblings(".reactions");
            $reactionsArea.empty();
            $.each(json.reactions, function (idx, reaction) {
                var user = reaction.userInfo;
                $reactionsArea.append(
                    $("<span>").append(
                        $("<img>").attr({
                            width: "24px",
                            height: "24px",
                            src: user.picture,
                            title: user.fullName
                        })
                    ).append(
                        $("<span>").text(user.fullName)
                    )
                ).append("\n");
            });
        });

    });

    var $eventTag = $(".event-tag-typeahead");
    var allTags = [];
    var tagMap = {};
    function resetTagSource(callback?: ()=>void) {
        Api.EventTag.getAllTags(function (json) {
            allTags = json.tags;
            $.each(allTags, function(i, tag) { tagMap[tag.text] = tag.id; });
            $eventTag
                .typeahead("destroy")
                .typeahead({
                    hint: true,
                    highlight: true,
                    minLength: 1
                },
                {
                    name: 'tags',
                    source: substringMatcher(allTags)
                });

            callback && callback();
        });
    }

    var $tagContainer = $(".event-tag-container");
    $tagContainer.on("click", ".tag-label > i", function () {
        var $this = $(this);
        var tag = $.trim($this.parent().text());
        selected.splice(selected.indexOf(tag), 1);
        $this.parent().remove();
    });
    var selected = [];
    (function lookupSelectedTags() {
        $(".event-tag-container .tag-label").each(function (idx, elem) {
            selected.push($.trim($(elem).text()));
        });
    }());
    function addTagIfNotExist (value: string) {
        return function (matches) {
            if (matches.length == 1) {
                var matched = matches[0];
                $eventTag.typeahead("val", matched).typeahead("close");
                selectTag(matched);
            } else if (matches.length == 0) {
                Api.EventTag.addTag(value, function () {
                    resetTagSource(function () {
                        selectTag(value);
                    });
                })
            }
        }
    }

    function selectTag(tag: string) {
        selected.push(tag);
        $eventTag.typeahead("val", "");
        $tagContainer.append(
            $("<span>").text(tag).addClass("tag-label label label-default")
                .append($("<i>").addClass("fa fa-close"))
                .append($("<input>").attr({
                    type: "hidden",
                    name: "tags[]",
                    value: tagMap[tag]
                }).addClass("fa fa-close"))
        );
        $eventTag.filter(".tt-input").focus();
    }
    $eventTag.on("keydown", function (ev) {
        if (ev.keyCode == 13) {
            ev.preventDefault();
            var value = $(this).val();
            substringMatcher(allTags)(value, addTagIfNotExist(value));
        }
    }).on('typeahead:select', function(ev, suggestion) {
        selectTag(suggestion);
    });

    function substringMatcher (tags) {
        return function findMatches(q, cb) {
            // an array that will be populated with substring matches
            var matches = [];

            // regex used to determine if a string contains the substring `q`
            var substrRegex = new RegExp(q, 'i');

            // iterate through the pool of strings and for any string that
            // contains the substring `q`, add it to the `matches` array
            $.each(tags, function(i, tag) {
                if($.inArray(tag.text, selected) < 0){
                    if (substrRegex.test(tag.text)) {
                        matches.push(tag.text);
                    }
                }
            });

            cb(matches);
        };
    }

    if (location.pathname != Alice.Utils.baseUrl) {
        resetTagSource();
    }

    var $dp =$('.datepicker');
    var $dpInput = $dp.find(".datepicker-input");
    var $dpHiddenData = $($dpInput.data("for"));
    var timestamp = parseInt($dpHiddenData.val());
    var datetimepickerOptions = (timestamp > 0)
        ? {format: "YYYY/MM/DD HH:mm", sideBySide: true, defaultDate: new Date(timestamp)}
        : {format: "YYYY/MM/DD HH:mm", sideBySide: true};
    $dp.datetimepicker(datetimepickerOptions)
        .on("dp.change", function (ev) {
            $dpHiddenData.val(ev.date ? ev.date.unix() * 1000 : 0);
        });

    $(".event-delete").on("click", function () {
        var $self = $(this);
        Alice.Modal.show({
            title: Utils.i18n("general.confirmation"),
            body: Utils.i18n("message.delete.confirmation.of.event", Utils.escapeHTML($self.data("eventTitle"))),
            singleButton : false,
            onOk: function() {
                Api.Event.deleteEvent($self.data("eventId"));
            }
        });
    })
});
