/** Copyright (C) 2014 - Anas H. Sulaiman (ahs.pw)
* 			All Rights Reserved.
*/

package services;

/**
 * Detects file type.
 * Author: Anas H. Sulaiman 
 */
public class TypeDetector {
    private static TypeDetector ourInstance = new TypeDetector();

    private TypeDetector() {
    }

    public static TypeDetector getInstance() {
        return ourInstance;
    }

    /**
     * A file is considered a document if it has one of the following extensions:
     * <pre>
     *     .txt
     *     .doc
     *     .txt
     *     .pdf
     *     .rtf
     * </pre>
     *
     * @param file
     * @return
     */
    public boolean isDocument(String file) {
        return false;
    }

    /**
     * A file is considered an image if it has one of the following extensions:
     * <pre>
     *     .jpg
     *     .jpeg
     *     .png
     *     .bmp
     *     .gif
     *     .tiff
     *     .psd
     *     .svg
     * </pre>
     *
     * @param file
     * @return
     */
    public boolean isImage(String file) {
        return false;
    }

    /**
     * A file is considered a video if it has one of the following extensions:
     * <pre>
     *     .mp4
     *     .mkv
     *     .flv
     *     .avi
     *     .mts
     *     .wmv
     * </pre>
     *
     * @param file
     * @return
     */
    public boolean isVideo(String file) {
        return false;
    }

    /**
     * A file is considered audio if it has one of the following extensions:
     * <pre>
     *     .mp3
     *     .wma
     *     .ogg
     *     .acc
     *     .wav
     *     .flac
     * </pre>
     *
     * @param file
     * @return
     */
    public boolean isAudio(String file) {
        return false;
    }

    public boolean isEmail(String data) {
        return false;
    }

    /**
     * A file is considered torrent if it has the extension .torrent.
     * A string that validates as magent link is considered torrent.
     *
     * @param data
     * @return
     */
    public boolean isTorrent(String data) {
        return false;
    }

    /**
     * A string is considered a url if it validates as one of the following protocols references:
     * <pre>
     *     http
     *     https
     *     ftp
     * </pre>
     *
     * @param data
     * @return
     */
    public boolean isUrl(String data) {
        return false;
    }

    /**
     * A string is considered an irc reference if validates as one.
     *
     * @param data
     * @return
     */
    public boolean isIrc(String data) {
        return false;
    }

    /**
     * A file is considered a subtitle if it has one of the following extensions:
     * <pre>
     *     .srt
     *     .ass
     * </pre>
     *
     * @param file
     * @return
     */
    public boolean isSubtitle(String file) {
        return false;
    }
}
