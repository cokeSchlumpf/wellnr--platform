package com.wellnr.platform.common.tuples;

import com.wellnr.platform.common.functions.Function1;
import com.wellnr.platform.common.functions.Procedure1;

import java.util.Objects;
import java.util.Optional;

public sealed abstract class Either<L, R> permits Left, Right {

    public static <L, R> Either<L, R> fromLeft(L value) {
        return Left.apply(value);
    }

    public static <L, R> Either<L, R> fromRight(R value) {
        return Right.apply(value);
    }

    public static <L, R> Either<L, R> apply(L left, R right) {
        if (!Objects.isNull(left)) {
            return Either.fromLeft(left);
        } else if (!Objects.isNull(right)) {
            return Either.fromRight(right);
        } else {
            throw new IllegalArgumentException("Either left or right must be not null, but both values are Null");
        }
    }

    public abstract boolean isLeft();

    public abstract boolean isRight();

    public abstract Optional<L> getLeft();

    public abstract Optional<R> getRight();

    public abstract <T> T map(Function1<L, T> mapLeft, Function1<R, T> mapRight);

    public abstract Either<L, R> ifLeft(Procedure1<L> ifLeft);

    public abstract Either<L, R> ifRight(Procedure1<R> ifRight);

}
