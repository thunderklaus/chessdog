package com.christophdietze.jack.client.presenter;

import com.allen_sauer.gwt.log.client.Log;
import com.christophdietze.jack.client.channel.Channel;
import com.christophdietze.jack.client.channel.ChannelFactory;
import com.christophdietze.jack.client.channel.SocketListener;
import com.christophdietze.jack.client.event.MatchEndedEvent;
import com.christophdietze.jack.client.event.MatchEndedEvent.Reason;
import com.christophdietze.jack.client.event.SignInFailedEvent;
import com.christophdietze.jack.client.event.SignedInEvent;
import com.christophdietze.jack.client.util.GlobalEventBus;
import com.christophdietze.jack.client.util.MyAsyncCallback;
import com.christophdietze.jack.shared.AbortResponse;
import com.christophdietze.jack.shared.ChessServiceAsync;
import com.christophdietze.jack.shared.CometMessage;
import com.christophdietze.jack.shared.CometService;
import com.christophdietze.jack.shared.LoginResponse;
import com.christophdietze.jack.shared.LoginResponse.LoginSuccessfulResponse;
import com.christophdietze.jack.shared.Player;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamFactory;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.inject.Inject;

public class CommandPresenter {

	public interface View {
		void update();
	}

	private GlobalEventBus eventBus;
	private ApplicationContext applicationContext;
	private GameManager gameManager;
	private ChessServiceAsync chessService;
	private CometMessageDispatcher cometMessageDispatcher;

	private View view;

	@Inject
	public CommandPresenter(GlobalEventBus eventBus, ApplicationContext applicationContext, GameManager gameManager,
			ChessServiceAsync chessService, CometMessageDispatcher cometMessageDispatcher) {
		this.eventBus = eventBus;
		this.applicationContext = applicationContext;
		this.gameManager = gameManager;
		this.chessService = chessService;
		this.cometMessageDispatcher = cometMessageDispatcher;
	}

	public void bindView(View view) {
		assert this.view == null;
		this.view = view;
		view.update();
	}

	public void onSignInClick(final String nickname) {
		login(nickname);
	}

	/*
	 * public void onPostPublicChallenge() { chessService.postSeek(applicationContext.getLocationId(), new
	 * MyAsyncCallback<PostSeekResponse>() {
	 * 
	 * @Override public void onSuccess(PostSeekResponse result) { switch (result) { case SUCCESS:
	 * Log.debug("You joined the seek list"); break; case ALREADY_SEEKING: Log.warn("You already have an active seek");
	 * break; case HAS_ACTIVE_MATCH: Log.warn("You cannot seek while you have an active match"); break; default: throw
	 * new AssertionError(); } view.update(); } }); }
	 */

	public void onAbortMatchClick() {
		chessService.abortMatch(applicationContext.getLocationId(), new MyAsyncCallback<AbortResponse>() {
			@Override
			public void onSuccess(AbortResponse result) {
				switch (result) {
				case SUCCESS:
					Log.debug("You aborted the game");
					gameManager.switchToAnalysisMode();
					eventBus.fireEvent(new MatchEndedEvent(Reason.YOU_ABORTED));
					break;
				case NO_ACTIVE_MATCH:
					Log.warn("You have no active match");
					break;
				default:
					throw new AssertionError();
				}
			}
		});
	}

	private void login(final String nickname) {
		chessService.login(nickname, new MyAsyncCallback<LoginResponse>() {
			@Override
			public void onSuccess(LoginResponse result) {
				assert result != null;
				switch (result.getType()) {
				case NICKNAME_ALREADY_EXISTS:
					eventBus.fireEvent(new SignInFailedEvent("Sign in failed. The nickname '" + nickname
							+ "' is already in use, please choose a different one."));
					break;
				case SUCCESS:
					LoginSuccessfulResponse successfulResponse = (LoginSuccessfulResponse) result;
					Player myPlayer = new Player(successfulResponse.getLocationId(), nickname);
					applicationContext.setMyPlayer(myPlayer);
					createChannelAndCompleteLogin(myPlayer, successfulResponse.getChannelId());
					break;
				default:
					throw new AssertionError();
				}
			}
		});
	}

	private void createChannelAndCompleteLogin(final Player myPlayer, String channelId) {
		Log.debug("Opening Channel " + channelId);
		Channel channel = ChannelFactory.createChannel(channelId);
		channel.open(new SocketListener() {
			@Override
			public void onOpen() {
				Log.debug("Channel opened, completing login");
				completeLogin(myPlayer);
			}
			@Override
			public void onMessage(String encodedMessage) {
				SerializationStreamFactory streamFactory = CometService.App.getSerializationStreamFactory();
				CometMessage message;
				try {
					SerializationStreamReader reader = streamFactory.createStreamReader(encodedMessage);
					message = (CometMessage) reader.readObject();
					Log.info("received CometMessage " + message);
				} catch (SerializationException ex) {
					throw new RuntimeException("SerializationException while deserializing encoded message '"
							+ encodedMessage + "'", ex);
				} catch (Throwable ex) {
					throw new RuntimeException("Unexpected error while deserializing encoded message '" + encodedMessage
							+ "'", ex);
				}
				cometMessageDispatcher.dispatch(message);
			}
		});
	}

	private void completeLogin(final Player myPlayer) {
		chessService.loginComplete(myPlayer.getLocationId(), new MyAsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				eventBus.fireEvent(new SignedInEvent(myPlayer));
			}
		});
	}
}