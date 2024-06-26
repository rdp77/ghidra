/* ###
 * IP: GHIDRA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ghidra.features.codecompare.decompile;

import ghidra.app.decompiler.ClangToken;
import ghidra.features.codecompare.graphanalysis.TokenBin;

public interface DiffClangHighlightListener {

	/**
	 * Notifier that the current location changed to the specified token.
	 * @param tok the token
	 * @param tokenBin the bin which contains the token. Otherwise, null.
	 */
	public void locationTokenChanged(ClangToken tok, TokenBin tokenBin);

}
