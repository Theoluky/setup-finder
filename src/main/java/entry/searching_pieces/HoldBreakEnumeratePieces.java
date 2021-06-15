package entry.searching_pieces;

import common.datastore.blocks.LongPieces;
import common.datastore.blocks.Pieces;
import common.order.ForwardOrderLookUp;
import common.pattern.PatternGenerator;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Break down from a combination with a hold into multiple combinations without a hold and remove duplicates
 */
public class HoldBreakEnumeratePieces implements EnumeratePiecesCore {
    private final PatternGenerator generator;
    private final int maxDepth;
    private int counter = -1;

    HoldBreakEnumeratePieces(PatternGenerator generator, int maxDepth) {
        assert maxDepth <= generator.getDepth();
        this.generator = generator;
        this.maxDepth = maxDepth;
    }

    @Override
    public Set<LongPieces> enumerate() throws IOException {
        assert counter == -1;

        int depth = generator.getDepth();
        ForwardOrderLookUp forwardOrderLookUp = new ForwardOrderLookUp(maxDepth, depth);

        AtomicInteger counter = new AtomicInteger();
        HashSet<LongPieces> searchingPieces = create(depth, forwardOrderLookUp, counter);

        this.counter = counter.get();
        return searchingPieces;
    }

    private HashSet<LongPieces> create(int depth, ForwardOrderLookUp forwardOrderLookUp, AtomicInteger counter) {
        if (maxDepth < depth)
            return createOverMinos(forwardOrderLookUp, counter);
        else
            return createJustMinos(forwardOrderLookUp, counter);
    }

    private HashSet<LongPieces> createJustMinos(ForwardOrderLookUp forwardOrderLookUp, AtomicInteger counter) {
        return generator.blocksStream()
                .peek(pieces -> counter.incrementAndGet())
                .map(Pieces::getPieces)
                .flatMap(forwardOrderLookUp::parse)
                .map(LongPieces::new)
                .collect(Collectors.toCollection(HashSet::new));
    }

    private HashSet<LongPieces> createOverMinos(ForwardOrderLookUp forwardOrderLookUp, AtomicInteger counter) {
        return generator.blocksStream()
                .peek(pieces -> counter.incrementAndGet())
                .map(Pieces::getPieces)
                .map(blocks -> blocks.subList(0, maxDepth + 1))  // ホールドありなので+1ミノ分使用する
                .flatMap(forwardOrderLookUp::parse)
                .map(LongPieces::new)
                .collect(Collectors.toCollection(HashSet::new));
    }

    @Override
    public int getCounter() {
        return counter;
    }
}
