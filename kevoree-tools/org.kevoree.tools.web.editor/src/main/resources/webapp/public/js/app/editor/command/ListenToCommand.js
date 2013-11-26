define(
    [
        'util/AlertPopupHelper',
        'kevoree'
    ],

    function (AlertPopupHelper, Kevoree) {

        function ListenToCommand() {}

        ListenToCommand.prototype.execute = function (editor, uri) {
            var ws = new WebSocket('ws://'+uri),
                loader = new Kevoree.org.kevoree.loader.JSONModelLoader(),
                serializer = new Kevoree.org.kevoree.serializer.JSONModelSerializer();

            ws.onmessage = function (event) {
                var model = loader.loadModelFromString(event.data).get(0);
                editor.setModel(model);
                loadSucceed(ws, uri);
            }

            ws.onopen = function () {
                // you are connected
                connectionSucceed(ws, uri);
            }

            ws.onclose = function () {
                connectionClosed();
            }

            ws.onerror = function () {
                loadFailed(uri);
            }

            editor.setModelListener({
                onUpdates: function () {
                    ws.send(serializer.serialize(editor.getModel()));
                }
            });
        }

        function connectionSucceed(ws, uri) {
            AlertPopupHelper.setHTML("Listening to "+uri+"...");
            AlertPopupHelper.setType(AlertPopupHelper.SUCCESS);
            AlertPopupHelper.show(3500);

            successPopupUI(uri, function () {
                ws.close();
            });
        }

        // private method
        function loadSucceed(ws, uri) {
            AlertPopupHelper.setText("New model received from ws://"+uri);
            AlertPopupHelper.setType(AlertPopupHelper.SUCCESS);
            AlertPopupHelper.show(3500);


            successPopupUI(uri, function () {
                ws.close();
            });
        }

        function loadFailed(uri) {
            AlertPopupHelper.setHTML("Unable to connect to "+uri+".<br/> Aborting listening process...");
            AlertPopupHelper.setType(AlertPopupHelper.ERROR);
            AlertPopupHelper.show(3500);

            errorPopupUI();
        }

        function connectionClosed() {
            AlertPopupHelper.setHTML('\"Listen to\" aborted. <br/> Connection closed.');
            AlertPopupHelper.setType(AlertPopupHelper.WARN);
            AlertPopupHelper.show(3500);

            errorPopupUI();
        }

        function successPopupUI(uri, closeCallback) {
            $('#listen-to-content').html('Currently listening to <span class="text-info">' + uri + '</span>');
            $('#listen-to-close').show();
            $('#listen-to-close').off('click');
            $('#listen-to-close').on('click', closeCallback);
            $('#listen-to').hide();
        }

        function errorPopupUI() {
            $('#listen-to-content').empty();
            $('#listen-to-close').hide();
            $('#listen-to').show();
        }

        return ListenToCommand;
    }
);