package org.unclesniper.confhoard.core.util;

import java.io.IOException;
import java.util.function.Function;
import org.unclesniper.confhoard.core.Fragment;
import org.unclesniper.confhoard.core.ConfStateBinding;
import org.unclesniper.confhoard.core.security.Credentials;

public class FragmentHolder implements AutoCloseable {

	private Fragment fragment;

	private final Credentials credentials;

	private final ConfStateBinding state;

	private final Function<String, Object> parameters;

	public FragmentHolder(Fragment fragment, Credentials credentials, ConfStateBinding state,
			Function<String, Object> parameters) {
		this.fragment = fragment;
		this.credentials = credentials;
		this.state = state;
		this.parameters = parameters;
	}

	public Fragment getFragment() {
		return fragment;
	}

	public void setFragment(Fragment fragment) {
		this.fragment = fragment;
	}

	public void release() {
		fragment = null;
	}

	@Override
	public void close() throws IOException {
		if(fragment != null)
			fragment.remove(credentials, state, parameters);
	}

}
