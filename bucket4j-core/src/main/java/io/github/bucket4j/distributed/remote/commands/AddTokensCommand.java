/*
 *
 * Copyright 2015-2018 Vladimir Bukhtoyarov
 *
 *       Licensed under the Apache License, Version 2.0 (the "License");
 *       you may not use this file except in compliance with the License.
 *       You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package io.github.bucket4j.distributed.remote.commands;

import io.github.bucket4j.Nothing;
import io.github.bucket4j.distributed.remote.CommandResult;
import io.github.bucket4j.distributed.remote.MutableBucketEntry;
import io.github.bucket4j.distributed.remote.RemoteBucketState;
import io.github.bucket4j.distributed.remote.RemoteCommand;
import io.github.bucket4j.serialization.DeserializationAdapter;
import io.github.bucket4j.serialization.SerializationAdapter;
import io.github.bucket4j.serialization.SerializationHandle;
import io.github.bucket4j.util.ComparableByContent;

import java.io.IOException;

public class AddTokensCommand implements RemoteCommand<Nothing>, ComparableByContent<AddTokensCommand> {

    private long tokensToAdd;

    public static SerializationHandle<AddTokensCommand> SERIALIZATION_HANDLE = new SerializationHandle<AddTokensCommand>() {
        @Override
        public <S> AddTokensCommand deserialize(DeserializationAdapter<S> adapter, S input) throws IOException {
            long tokensToAdd = adapter.readLong(input);

            return new AddTokensCommand(tokensToAdd);
        }

        @Override
        public <O> void serialize(SerializationAdapter<O> adapter, O output, AddTokensCommand command) throws IOException {
            adapter.writeLong(output, command.tokensToAdd);
        }

        @Override
        public int getTypeId() {
            return 24;
        }

        @Override
        public Class<AddTokensCommand> getSerializedType() {
            return AddTokensCommand.class;
        }

    };

    public AddTokensCommand(long tokensToAdd) {
        this.tokensToAdd = tokensToAdd;
    }

    @Override
    public CommandResult<Nothing> execute(MutableBucketEntry mutableEntry, long currentTimeNanos) {
        if (!mutableEntry.exists()) {
            return CommandResult.bucketNotFound();
        }
        RemoteBucketState state = mutableEntry.get();
        state.refillAllBandwidth(currentTimeNanos);
        state.addTokens(tokensToAdd);
        mutableEntry.set(state);
        return CommandResult.NOTHING;
    }

    public long getTokensToAdd() {
        return tokensToAdd;
    }

    @Override
    public SerializationHandle getSerializationHandle() {
        return SERIALIZATION_HANDLE;
    }

    @Override
    public boolean equalsByContent(AddTokensCommand other) {
        return tokensToAdd == other.tokensToAdd;
    }

}