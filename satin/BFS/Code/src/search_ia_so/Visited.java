/*
 * Copyright 2017 Patrick Goddijn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package search_ia_so;

final class Visited extends ibis.satin.SharedObject implements VisitedInterface {
    private boolean[] visited;

    public void set(boolean[] v) {
        visited = v;
    }

    public boolean get(int i) {
        return visited[i];
    }

    public void update(int index) {
        visited[index] = true;
    }

    public void localUpdate(int index) {
        visited[index] = true;
    }

    public void update_list(int[] updates) {
        for(int i : updates) {
            visited[i] = true;
        }
    }
}
