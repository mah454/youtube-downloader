package ir.moke.youtube;

public interface Calculator {
    static long percentage(long total, long obtain) {
        return (obtain * 100) / total;
    }
}
