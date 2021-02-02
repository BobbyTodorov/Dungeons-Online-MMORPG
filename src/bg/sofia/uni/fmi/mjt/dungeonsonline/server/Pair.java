package bg.sofia.uni.fmi.mjt.dungeonsonline.server;

public final class Pair<L, R> {
    private L leftElement;
    private R rightElement;

    public Pair(L leftElement, R rightElement) {
        setLeftElement(leftElement);
        setRightElement(rightElement);
    }

    public void setLeftElement(L leftElement) {
        this.leftElement = leftElement;
    }

    public void setRightElement(R rightElement) {
        this.rightElement = rightElement;
    }

    public L getLeftElement() {
        return leftElement;
    }

    public R getRightElement() {
        return rightElement;
    }
}
