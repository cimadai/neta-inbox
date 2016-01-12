/// <reference path="./third_party/jquery/jquery.d.ts" />
/// <reference path="./alice.common.ts" />
/// <reference path="./third_party/bootstrap/bootstrap.d.ts" />

namespace Alice {

    var defaultOptions = {
        title: "",
        body: "",
        bodyElem: $('<span>'),
        okTextKey : "general.ok",
        cancelTextKey : "general.cancel",
        primary: 0,
        onOpen: function() {},
        onOk: function () {},
        onCancel: function () {},
        singleButton: false,
        fade: true,
        backdrop: false,
        show: true,
        keyboard: true,
        error: false
    };

    var modalDom =
        $('<div id="alice_modal_dialog">')
            .addClass('modal fade')
            .append(
            $('<div>')
                .addClass('modal-dialog')
                .append($('<div>')
                    .addClass('modal-content')
                    .append(
                    $('<div class="modal-header">')
                        //.append('<a href="#" class="close">&times;</a>')
                        .append('<h3 id="alice_modal_header"></h3>')
                )
                    .append(
                    $('<div class="modal-body">')
                        .append('<p><span id="alice_modal_body"></span></p>')
                )
                    .append(
                    $('<div class="modal-footer">')
                        .append($('<button>').attr('id', 'alice_modal_ok').addClass('btn').addClass('btn-primary'))
                        .append($('<button>').attr('id', 'alice_modal_cancel').addClass('btn'))
                )
            )
        )
            .appendTo('body');

    function createProxy(func) {
        return function () {
            var closable = true;
            if (Alice.Utils.isFunction(func)) {
                closable = func();
            }
            if (closable !== false) {
                _hide();
            }
        };
    }

    function _show(options) {
        var opt = $.extend({}, defaultOptions, options);

        modalDom
            .removeClass("error-modal")
            .find("#alice_modal_header")
                .text(opt.title)
                .end() // end of find
            .find("#alice_modal_body")
                .html(opt.body)
                .append(opt.bodyElem)
                .end() // end of find
            .find(".btn-primary")
                .removeClass("btn-primary")
                .end() // end of find
            .find(".modal-footer button")
                .eq(opt.primary)
                    .addClass("btn-primary")
                    .end() // end of eq
                .end() // end of find
            .find("#alice_modal_ok")
                .off('click')
                .click(createProxy(opt.onOk))
                .text(Alice.Utils.i18n(opt.okTextKey))
                .end() // end of find
            .find("#alice_modal_cancel")
                .css("display", "inline")
                .off('click')
                .click(createProxy(opt.onCancel))
                .text(Alice.Utils.i18n(opt.cancelTextKey))
                .end() // end of find
            .find(".close")
                .off('click')
                .click(createProxy(opt.onCancel))
                .end(); // end of find

        if (opt.error) {
            modalDom.addClass("error-modal")
        }
        if (opt.fade) {
            modalDom.addClass('fade')
        }
        if (opt.singleButton) {
            modalDom.find("#alice_modal_cancel").css("display", "none");
        }

        modalDom.modal(opt);

        opt.onOpen();
    }

    function _hide() {
        modalDom.modal("hide");
    }

    export var Modal = {
        show: _show,
        hide: _hide
    };

};
