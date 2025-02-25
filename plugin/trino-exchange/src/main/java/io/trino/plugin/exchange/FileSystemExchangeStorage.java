/*
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
package io.trino.plugin.exchange;

import com.google.common.util.concurrent.ListenableFuture;

import javax.crypto.SecretKey;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

public interface FileSystemExchangeStorage
        extends AutoCloseable
{
    void createDirectories(URI dir) throws IOException;

    ExchangeStorageReader createExchangeStorageReader(Queue<ExchangeSourceFile> sourceFiles, int maxPageStorageSize);

    ExchangeStorageWriter createExchangeStorageWriter(URI file, Optional<SecretKey> secretKey);

    boolean exists(URI file) throws IOException;

    ListenableFuture<Void> createEmptyFile(URI file);

    ListenableFuture<Void> deleteRecursively(URI dir);

    List<FileStatus> listFiles(URI dir) throws IOException;

    List<URI> listDirectories(URI dir) throws IOException;

    int getWriteBufferSize();

    @Override
    void close() throws IOException;
}
