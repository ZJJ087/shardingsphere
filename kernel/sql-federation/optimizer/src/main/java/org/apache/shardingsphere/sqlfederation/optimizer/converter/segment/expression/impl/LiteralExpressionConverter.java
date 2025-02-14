/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.expression.impl;

import org.apache.calcite.sql.SqlLiteral;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.fun.SqlTrimFunction.Flag;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.SQLSegmentConverter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

/**
 * Literal expression converter.
 */
public final class LiteralExpressionConverter implements SQLSegmentConverter<LiteralExpressionSegment, SqlNode> {
    
    private static final Collection<String> TRIM_FUNCTION_FLAGS = new HashSet<>(3, 1);
    
    static {
        TRIM_FUNCTION_FLAGS.add("BOTH");
        TRIM_FUNCTION_FLAGS.add("LEADING");
        TRIM_FUNCTION_FLAGS.add("TRAILING");
    }
    
    @Override
    public Optional<SqlNode> convert(final LiteralExpressionSegment segment) {
        if (null == segment.getLiterals()) {
            return Optional.of(SqlLiteral.createNull(SqlParserPos.ZERO));
        }
        if (segment.getLiterals() instanceof Integer) {
            return Optional.of(SqlLiteral.createExactNumeric(String.valueOf(segment.getLiterals()), SqlParserPos.ZERO));
        }
        if (TRIM_FUNCTION_FLAGS.contains(String.valueOf(segment.getLiterals()))) {
            return Optional.of(SqlLiteral.createSymbol(Flag.valueOf(String.valueOf(segment.getLiterals())), SqlParserPos.ZERO));
        }
        if (segment.getLiterals() instanceof String) {
            return Optional.of(SqlLiteral.createCharString(String.valueOf(segment.getLiterals()), SqlParserPos.ZERO));
        }
        return Optional.empty();
    }
}
