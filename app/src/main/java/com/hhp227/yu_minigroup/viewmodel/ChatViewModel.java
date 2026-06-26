package com.hhp227.yu_minigroup.viewmodel;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.data.ChatRepository;
import com.hhp227.yu_minigroup.dto.MessageItem;
import com.hhp227.yu_minigroup.dto.User;
import com.hhp227.yu_minigroup.helper.Callback;
import com.hhp227.yu_minigroup.helper.PreferenceManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ChatViewModel extends ViewModel {
    private static final int LIMIT = 15;

    public final MutableLiveData<String> inputMessage;

    private final MutableLiveData<List<MessageItem>> mMessageItemList = new MutableLiveData<>(Collections.emptyList());

    private final List<MessageItem> mMessageItems = new ArrayList<>();

    private final MutableLiveData<ScrollEvent> mScrollEvent = new MutableLiveData<>();

    private final PreferenceManager mPreferenceManager = AppController.getInstance().getPreferenceManager();

    private final ChatRepository mChatRepository = new ChatRepository();

    private final SavedStateHandle mSavedStateHandle;

    private String mReceiver;

    private boolean mIsGroupChat;

    private String mCursor;

    private String mFirstMessageKey;

    private boolean mHasRequestedMore;

    public ChatViewModel(SavedStateHandle savedStateHandle) {
        mSavedStateHandle = savedStateHandle;
        mReceiver = savedStateHandle.get("uid");
        Boolean isGroupChat = savedStateHandle.get("grp_chat");
        mIsGroupChat = isGroupChat != null && isGroupChat;
        inputMessage = savedStateHandle.getLiveData("inputMessage", "");

        if (mReceiver != null) {
            fetchInitialPage();
        }
    }

    public User getUser() {
        return mPreferenceManager.getUser();
    }

    public LiveData<List<MessageItem>> getMessageItemList() {
        return mMessageItemList;
    }

    public LiveData<ScrollEvent> getScrollEvent() {
        return mScrollEvent;
    }

    public LiveData<InputMessageFormState> getMessageFormState() {
        return mSavedStateHandle.getLiveData("messageFormState");
    }

    public boolean hasRequestedMore() {
        return mHasRequestedMore;
    }

    public void fetchInitialPage() {
        if (mReceiver != null) {
            fetchMessageList(0, null);
        }
    }

    public void fetchPreviousPage() {
        if (!mHasRequestedMore && mCursor != null) {
            mHasRequestedMore = true;
            fetchMessageList(getMessageCount(), mCursor);
            mCursor = null;
        }
    }

    public void actionSend() {
        String message = inputMessage.getValue();

        if (TextUtils.isEmpty(message) || message.trim().length() == 0) {
            mSavedStateHandle.set("messageFormState", new InputMessageFormState("메시지를 입력하세요."));
            return;
        }
        if (mReceiver == null) {
            mSavedStateHandle.set("messageFormState", new InputMessageFormState("채팅 대상 정보가 없습니다."));
            return;
        }
        String trimmedMessage = message.trim();

        mChatRepository.sendMessage(getUser(), mReceiver, mIsGroupChat, trimmedMessage);
        addSentMessage(trimmedMessage);
        inputMessage.setValue("");
    }

    private void addSentMessage(String message) {
        User user = getUser();

        mMessageItems.add(new MessageItem(user.getUid(), user.getName(), message, "text", false, System.currentTimeMillis()));
        mMessageItemList.setValue(new ArrayList<>(mMessageItems));
        mScrollEvent.setValue(new ScrollEvent(true, 0, mMessageItems.size(), false));
    }

    private void fetchMessageList(final int previousCount, final String previousCursor) {
        mChatRepository.fetchMessageList(getUser().getUid(), mReceiver, mIsGroupChat, previousCursor, LIMIT, new Callback() {
            @Override
            public <T> void onSuccess(T data) {
                List<Map.Entry<String, MessageItem>> messageItemList = (List<Map.Entry<String, MessageItem>>) data;
                int insertPosition = Math.max(getMessageCount() - previousCount, 0);
                int addedCount = 0;
                String newCursor = null;

                for (Map.Entry<String, MessageItem> entry : messageItemList) {
                    String key = entry.getKey();
                    MessageItem messageItem = entry.getValue();

                    if (key == null || messageItem == null) {
                        continue;
                    }
                    if (newCursor == null) {
                        newCursor = key;
                    }
                    if (mFirstMessageKey != null && mFirstMessageKey.equals(key)) {
                        continue;
                    } else if (mFirstMessageKey == null) {
                        mFirstMessageKey = key;
                    }
                    if (key.equals(previousCursor)) {
                        continue;
                    }
                    mMessageItems.add(insertPosition + addedCount, messageItem);
                    addedCount++;
                }
                mHasRequestedMore = false;
                if (newCursor != null) {
                    mCursor = newCursor;
                }
                if (addedCount == 0) {
                    return;
                }
                mMessageItemList.setValue(new ArrayList<>(mMessageItems));
                mScrollEvent.setValue(new ScrollEvent(previousCount == 0, addedCount, mMessageItems.size(), previousCount > 0));
            }

            @Override
            public void onFailure(Throwable throwable) {
                mHasRequestedMore = false;
                mSavedStateHandle.set("messageFormState", new InputMessageFormState(throwable.getMessage()));
            }

            @Override
            public void onLoading() {
            }
        });
    }

    private int getMessageCount() {
        return mMessageItems.size();
    }

    public static final class ScrollEvent {
        public final boolean initialLoad;

        public final int addedCount;

        public final int itemCount;

        public final boolean requestedMore;

        public ScrollEvent(boolean initialLoad, int addedCount, int itemCount, boolean requestedMore) {
            this.initialLoad = initialLoad;
            this.addedCount = addedCount;
            this.itemCount = itemCount;
            this.requestedMore = requestedMore;
        }
    }

    public static final class InputMessageFormState implements Parcelable {
        public String messageError;

        public InputMessageFormState(String messageError) {
            this.messageError = messageError;
        }

        protected InputMessageFormState(Parcel in) {
            messageError = in.readString();
        }

        public static final Creator<InputMessageFormState> CREATOR = new Creator<InputMessageFormState>() {
            @Override
            public InputMessageFormState createFromParcel(Parcel in) {
                return new InputMessageFormState(in);
            }

            @Override
            public InputMessageFormState[] newArray(int size) {
                return new InputMessageFormState[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(messageError);
        }
    }
}
