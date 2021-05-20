var MultiPlayer = (function () {
    function MultiPlayerConstruct() {
        // https://developer.android.com/reference/android/media/AudioManager.html
        this.STREAM_MUSIC = 3;
        this.STREAM_ALARM = 4;
    }
    const noop = () => { };

    const exec = (successCallback = noop, failureCallback = noop, name, args) => {
        cordova.exec(successCallback, failureCallback, 'MultiPlayer', name, args);
    };

    const methods = {
        initialize: null,
        connect: null,
        disconnect: null,
        play(streamType) {
            if (typeof streamType == 'undefined') {
                streamType = -1;
            }
            return [streamType];
        },
        stop: null,
        getDuration: null,
    };

    for (let key in methods) {
        MultiPlayerConstruct.prototype[key] = function(successCallback = noop, failureCallback = noop, ...args) {
            const func = methods[key];
            if (func) {
                args = func(...args);
            }
            exec(successCallback, failureCallback, key, args);
        }
    }

    return new MultiPlayerConstruct();
})();

module.exports = MultiPlayer;
