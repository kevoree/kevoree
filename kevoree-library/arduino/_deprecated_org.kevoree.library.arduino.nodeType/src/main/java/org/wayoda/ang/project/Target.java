/**
 * The MIT License for the "Ang Build Tool"
 * 
 * Copyright (c) <2009> Eberhard Fahle <e.fahle@wayoda.org>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * 
 */

package org.wayoda.ang.project;

/**
 * Class Target stores the basic properties of the different 
 * boards supported by the Arduino project.
 * Besides the board-properties that are read from the boards.txt
 * file of the Arduino-project, an instance of Target also 
 * provides methods that return the fuse-settings for burning  
 * a bootloader or uploading code with an in-system-programmer.
 */
public class Target {

    private String key;
    private String name;
    private String mcu;
    private String speed;
    private String core;
    private String uploadSize;
    private String uploadSpeed;

    /**
     * Creates a new instance of class Target. Standard Targets are created by the 
     * {@link org.wayoda.ang.project.TargetList} constructor. 
     * Applications can than retrieve instances of Targets with method
     * {@link org.wayoda.ang.project.TargetList#getTarget(String)}
     * @param key The key under which this board registered
     * @param name An informal name for the board
     * @param mcu The type of microcontroller on this board
     * @param speed The speed for this board 
     * @param core The core group this target belongs to
     * @param uploadSize The maximum size for an image to upload
     * @param uploadSpeed The maximum speed for uploading over the serial line
     * @throws IllegalArgumentException if any one of the settings is null or the empty String which
     * would lead to an incomplete definition for the board.
     */
    Target(String key, String name, String mcu, String speed, String core, String uploadSize, String uploadSpeed) {

        if ( key == null || key.equals( "" ) ) {
            throw new IllegalArgumentException( "Incomplete Target definition for `key`" );
        }
        if ( name == null || name.equals( "" ) ) {
            throw new IllegalArgumentException( "Incomplete Target definition for `name`" );
        }
        if ( mcu == null || mcu.equals( "" ) ) {
            throw new IllegalArgumentException( "Incomplete Target definition for `mcu`" );
        }
        if ( speed == null || speed.equals( "" ) ) {
            throw new IllegalArgumentException( "Incomplete Target definition for `f_cpu`" );
        }
        if ( core == null || core.equals( "" ) ) {
            throw new IllegalArgumentException( "Incomplete Target definition for `core`" );
        }
        if ( uploadSize == null || uploadSize.equals( "" ) ) {
            throw new IllegalArgumentException( "Incomplete Target definition for `upload size`" );
        }
        if ( uploadSpeed == null || uploadSpeed.equals( "" ) ) {
            throw new IllegalArgumentException( "Incomplete Target definition for `upload speed`" );
        }
        this.key = key;
        this.name = name;
        this.mcu = mcu;
        this.speed = speed;
        this.core = core;
        this.uploadSize = uploadSize;
        this.uploadSpeed = uploadSpeed;
    }

    /**
     * Gets the basic key for this target
     * @return String the key for this target
     */
    public String getKey() {

        return key;
    }

    /**
     * Gets the processor type for a target
     * @return String the type of processor used on this target. 
     * Returns null, if the property is not found or the type 
     * of target is unknown.
     */
    public String getMCU() {

        return mcu;
    }

    /**
     * Gets the processor speed for a target
     * @return String the speed of the processor used on this target. 
     * Returns null, if the property is not found or the type 
     * of target is unknown.
     */
    public String getSpeed() {

        return speed;
    }

    /**
     * Gets the core type for a target
     * @return String the core core for which the sourcefiles must be compiled 
     */
    public String getCore() {

        return core;
    }

    /**
     * Gets the informal name for the target
     * @return String the informal name of this target. 
     */
    public String getName() {

        return name;
    }

    /**
     * Gets the maximum upload size for the target
     * @return int the maximum num,ber of bytes for an image to be uploaded to this target. 
     */
    public int getUploadSize() {

        return Integer.parseInt( uploadSize );
    }

    /**
     * Gets the maximum upload speed for the target
     * @return String the maximum speed to be used when uploading 
     * an image to this target. 
     */
    public String getUploadSpeed() {

        return uploadSpeed;
    }
}
