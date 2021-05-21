var MultiPlayer = (function () {
    function MultiPlayerConstruct() {
        // https://developer.android.com/reference/android/media/AudioManager.html
        this.STREAM_MUSIC = 3;
        this.STREAM_ALARM = 4;
    }
    const exec = (successCallback, failureCallback, name, args) => {
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
        getCurrentPosition: null,
        seekTo: null,
    };

    for (let key in methods) {
        MultiPlayerConstruct.prototype[key] = function(...args) {
            const func = methods[key];
            if (typeof args[0] === 'function') {
                // callback pattern
                const successCallback = args[0];
                const failureCallback = args[1];
                const func = methods[key];
                if (func) {
                    args = func(...args.slice(2));
                }
                exec(successCallback, failureCallback, key, args);
            } else {
                // promise pattern
                return new Promise((resolve, reject) => {
                    if (func) {
                        args = func(...args);
                    }
                    exec(resolve, reject, key, args);
                });
            }
        }
    }

    return new MultiPlayerConstruct();
})();

module.exports = MultiPlayer;
