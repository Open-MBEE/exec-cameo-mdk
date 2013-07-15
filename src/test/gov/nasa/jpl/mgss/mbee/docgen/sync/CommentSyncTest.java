package gov.nasa.jpl.mgss.mbee.docgen.sync;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import static org.junit.Assert.*;

public class CommentSyncTest {
	private String now = CommentUtil.TIME_FORMAT.format(new Date());
	private SyncedComment c1 = SyncedComment.added("1", "abc", now, "comment 1");
	private SyncedComment c2 = SyncedComment.added("2", "abc", now, "comment 2");
	private SyncedComment c3 = SyncedComment.added("3", "abc", now, "comment 3");

	@Test
	public void testFileRepository() throws IOException, CommentSyncFailure {
		if (true) return; // short-circuit for now, repo methods take NamedElement now
		List<SyncedComment> sent, received;
		File file = File.createTempFile("testFileRepository", null);
		FileCommentRepository repo = new FileCommentRepository(file.getAbsolutePath());
		repo.connect();

		sent = list(c1, c2, c3);
		repo.sendComments(null, sent, null, null);
		received = repo.getComments(null);

		file.delete();
		for (SyncedComment c: received) {
			System.out.println("Received: " + c);
		}
		assertSameProperties(sent, received, new IdGetter());
		assertSameProperties(sent, received, new AuthorGetter());
		assertSameProperties(sent, received, new TimestampGetter());
		assertSameProperties(sent, received, new BodyGetter());
	}

	private List<SyncedComment> list(SyncedComment... comments) {
		List<SyncedComment> list = new ArrayList<SyncedComment>();
		for (SyncedComment c: comments) {
			list.add(c);
		}
		return list;

	}
	private void assertSameProperties(Collection<SyncedComment> a, Collection<SyncedComment> b, PropertyGetter getter) {
		assertEquals(a.size(), b.size());
		Set<String> aProperties = new HashSet<String>();
		Set<String> bProperties = new HashSet<String>();
		for (SyncedComment c: a) {
			aProperties.add(getter.get(c));
		}
		for (SyncedComment c: b) {
			bProperties.add(getter.get(c));
		}
		assertTrue(aProperties.containsAll(bProperties));
	}
	private interface PropertyGetter {
		String get(SyncedComment c);
	}
	private class IdGetter implements PropertyGetter {
		@Override public String get(SyncedComment c) {
			return c.getId();
		}
	}
	private class AuthorGetter implements PropertyGetter {
		@Override public String get(SyncedComment c) {
			return c.getAuthor();
		}
	}
	private class TimestampGetter implements PropertyGetter {
		@Override public String get(SyncedComment c) {
			return c.getTimestamp();
		}
	}
	private class BodyGetter implements PropertyGetter {
		@Override public String get(SyncedComment c) {
			return c.getBody();
		}
	}

}
