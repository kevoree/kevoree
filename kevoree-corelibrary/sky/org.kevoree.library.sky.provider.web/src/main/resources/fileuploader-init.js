$(document).ready(function () {
    var manualuploader = new qq.FileUploader({
        element:document.getElementById('manual-fine-uploader'),
        action:'do-nothing.htm',
        autoUpload:false,
        debug:true,
        uploadButtonText:'<span class="icon-upload icon-white" style="font-style: italic;"></span> Test me now and upload a file',
        template:'<div class="qq-uploader" style="width: 300px;">' +
            '<pre class="qq-upload-drop-area" style="width: 300px;"><span>{dragText}</span></pre>' +
            '<div class="qq-upload-button btn btn-success" style="width: auto;">{uploadButtonText}</div>' +
            '<ul class="qq-upload-list" style="margin-top: 10px; text-align: center;"></ul>' +
            '</div>',
        classes:{
            button:'qq-upload-button',
            drop:'qq-upload-drop-area',
            dropActive:'qq-upload-drop-area-active',
            dropDisabled:'qq-upload-drop-area-disabled',
            list:'qq-upload-list',
            progressBar:'qq-progress-bar',
            file:'qq-upload-file',
            spinner:'qq-upload-spinner',
            finished:'qq-upload-finished',
            size:'qq-upload-size',
            cancel:'qq-upload-cancel',
            failText:'qq-upload-failed-text',
            success:'alert alert-success',
            fail:'alert alert-error',
            successIcon:null,
            failIcon:null
        }
    });

    $('#triggerUpload').click(function () {
        manualuploader.uploadStoredFiles();
    });
})