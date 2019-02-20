/*  NetBare - An android network capture and injection library.
 *  Copyright (C) 2018-2019 Megatron King
 *  Copyright (C) 2018-2019 GuoShi
 *
 *  NetBare is free software: you can redistribute it and/or modify it under the terms
 *  of the GNU General Public License as published by the Free Software Found-
 *  ation, either version 3 of the License, or (at your option) any later version.
 *
 *  NetBare is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 *  PURPOSE. See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with NetBare.
 *  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.megatronking.netbare;

/**
 * Interface definition for a callback to be invoked when the NetBare service state changes.
 *
 * @author Megatron King
 * @since 2018-10-11 19:44
 */
public interface NetBareListener {

    /**
     * Callback method to be invoked when the NetBare service is started. It usual is called after
     * {@link NetBare#start(NetBareConfig)}.
     */
    void onServiceStarted();

    /**
     * Callback method to be invoked when the NetBare service is stopped. It usual is called after
     * {@link NetBare#stop()} or another VPN service is established.
     */
    void onServiceStopped();

}
