package javax.safetycritical;

//@SCJAllowed
public interface Safelet {

    // @SCJAllowed
    // @SCJRestricted( { INITIALIZATION })
    public MissionSequencer getSequencer();

    // @SCJAllowed
    public void setUp();

    // @SCJAllowed
    public void tearDown();
}
