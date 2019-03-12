package cs455.scaling.util;

import java.util.Arrays;
import java.util.Random;

public class MessageMaker {

    static int tracker = 0;

    public static byte []createMessage() {
        Random random = new Random(); // slow to use this staticly?
        byte []message = new byte[8 * 1024];
        random.nextBytes(message);

        return message;
    }


    public static byte []readableMessage() {
        byte []lol = "Someone must have been telling lies about Josef K., he knew he had done nothing wrong but, one morning, he was arrested. Every day at eight in the morning he was brought his breakfast by Mrs. Grubach's cook - Mrs. Grubach was his landlady - but today she didn't come. That had never happened before. K. waited a little while, looked from his pillow at the old woman who lived opposite and who was watching him with an inquisitiveness quite unusual for her, and finally, both hungry and disconcerted, rang the bell. There was immediately a knock at the door and a man entered. He had never seen the man in this house before. He was slim but firmly built, his clothes were black and close-fitting, with many folds and pockets, buckles and buttons and a belt, all of which gave the impression of being very practical but without making it very clear what they were actually for. \"Who are you?\" asked K., sitting half upright in his bed. The man, however, ignored the question as if his arrival simply had to be accepted, and merely replied, \"You rang?\" \"Anna should have brought me my breakfast,\" said K. He tried to work out who the man actually was, first in silence, just through observation and by thinking about it, but the man didn't stay still to be looked at for very long. Instead he went over to the door, opened it slightly, and said to someone who was clearly standing immediately behind it, \"He wants Anna to bring him his breakfast.\" There was a little laughter in the neighbouring room, it was not clear from the sound of it whether there were several people laughing. The strange man could not have learned anything from it that he hadn't known already, but now he said to K., as if making his report \"It is not possible.\" \"It would be the first time that's happened,\" said K., as he jumped out of bed and quickly pulled on his trousers. \"I want to see who that is in the next room, and why it is that Mrs. Grubach has let me be disturbed in this way.\" It immediately occurred to him that he needn't have said this out loud, and that he must to some extent have acknowledged their authority by doing so, but that didn't seem important to him at the time. That, at least, is how the stranger took it, as he said, \"Don't you think you'd better stay where you are?\" \"I want neither to stay here nor to be spoken to by you until you've introduced yourself.\" \"I meant it for your own good,\" said the stranger and opened the door, this time without being asked. The next room, which K. entered more slowly than he had intended, looked at first glance exactly the same as it had the previous evening. It was Mrs. Grubach's living room, over-filled with furniture, tablecloths, porcelain and photographs. Perhaps there was a little more space in there than usual today, but if so it was not immediately obvious, especially as the main difference was the presence of a man sitting by the open window with a book from which he now looked up. \"You should have stayed in your room! Didn't Franz tell you?\" \"And what is it you want, then?\" said K., looking back and forth between this new acquaintance and the one named Franz, who had remained in the doorway. Through the open window he noticed the old woman again, who had come close to the window opposite so that she could continue to see everything.".getBytes();

        byte []message = Arrays.copyOfRange(lol, tracker, Math.min(tracker + 40, lol.length));

        tracker = (tracker + 40) % lol.length;

        return message;
    }

    public static byte []readableMessage2() {
        byte []lol = "この部屋は、彼らの家が不衛生とまでは言わないまでも、すすけたロンドンの通りに面していることを考えると、ことのほか清潔で手入れが行き届いていた。ふらりと訪れた客、特にバンティング夫婦より上の階級に属する客".getBytes();

        byte []message = Arrays.copyOfRange(lol, tracker, tracker + 40);

        tracker = (tracker + 40) % lol.length;

        if (tracker + 40 > lol.length) {
            tracker = 0;
        }

        return message;
    }
}
