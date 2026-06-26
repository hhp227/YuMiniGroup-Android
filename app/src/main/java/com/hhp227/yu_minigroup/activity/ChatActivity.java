package com.hhp227.yu_minigroup.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hhp227.yu_minigroup.adapter.MessageListAdapter;
import com.hhp227.yu_minigroup.databinding.ActivityChatBinding;
import com.hhp227.yu_minigroup.dto.MessageItem;
import com.hhp227.yu_minigroup.viewmodel.ChatViewModel;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private boolean mHasSelection;

    private MessageListAdapter mAdapter;

    private View.OnLayoutChangeListener mOnLayoutChangeListener;

    private ActivityChatBinding mBinding;

    private ChatViewModel mViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityChatBinding.inflate(getLayoutInflater());
        mViewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        mAdapter = new MessageListAdapter(new ArrayList<MessageItem>(), mViewModel.getUser().getUid());

        setContentView(mBinding.getRoot());
        setSupportActionBar(mBinding.toolbar);
        if (getSupportActionBar() != null) {
            boolean isGroupChat = getIntent().getBooleanExtra("grp_chat", false);

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getIntent().getStringExtra("chat_nm") + (isGroupChat ? " 그룹채팅방" : ""));
        }
        subscribeUi();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mOnLayoutChangeListener != null) {
            mBinding.rvMessage.removeOnLayoutChangeListener(mOnLayoutChangeListener);
        }
        mBinding.rvMessage.clearOnScrollListeners();
        mOnLayoutChangeListener = null;
        mBinding = null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void subscribeUi() {
        mBinding.setViewModel(mViewModel);
        mBinding.setLifecycleOwner(this);
        mBinding.rvMessage.setAdapter(mAdapter);
        mOnLayoutChangeListener = (v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (bottom < oldBottom && mHasSelection && mAdapter.getItemCount() > 0) {
                mBinding.rvMessage.post(() -> mBinding.rvMessage.scrollToPosition(mAdapter.getItemCount() - 1));
            }
        };
        mBinding.rvMessage.addOnLayoutChangeListener(mOnLayoutChangeListener);
        mBinding.rvMessage.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!mBinding.rvMessage.canScrollVertically(-1) && !mViewModel.hasRequestedMore()) {
                    mViewModel.fetchPreviousPage();
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                if (layoutManager != null) {
                    mHasSelection = layoutManager.findFirstCompletelyVisibleItemPosition() + layoutManager.getChildCount() > layoutManager.getItemCount() - 2;
                }
            }
        });
        mViewModel.getMessageItemList().observe(this, new Observer<List<MessageItem>>() {
            @Override
            public void onChanged(List<MessageItem> messageItems) {
                mAdapter.submitList(messageItems);
            }
        });
        mViewModel.getScrollEvent().observe(this, new Observer<ChatViewModel.ScrollEvent>() {
            @Override
            public void onChanged(ChatViewModel.ScrollEvent scrollEvent) {
                if (scrollEvent == null || (!scrollEvent.initialLoad && !mHasSelection && !scrollEvent.requestedMore)) {
                    return;
                }
                try {
                    if (scrollEvent.initialLoad) {
                        mBinding.rvMessage.scrollToPosition(scrollEvent.itemCount - 1);
                    } else {
                        LinearLayoutManager layoutManager = (LinearLayoutManager) mBinding.rvMessage.getLayoutManager();

                        if (layoutManager != null) {
                            layoutManager.scrollToPositionWithOffset(scrollEvent.addedCount, 10);
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        });
        mViewModel.getMessageFormState().observe(this, new Observer<ChatViewModel.InputMessageFormState>() {
            @Override
            public void onChanged(ChatViewModel.InputMessageFormState inputMessageFormState) {
                if (inputMessageFormState != null && inputMessageFormState.messageError != null) {
                    Toast.makeText(ChatActivity.this, inputMessageFormState.messageError, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
