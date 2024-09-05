// #if __has_include(<RCTText/RCTBaseTextInputView.h>)
// #import <RCTText/RCTBaseTextInputView.h>
// #else
#import <React/RCTBaseTextInputView.h>
// #endif

NS_ASSUME_NONNULL_BEGIN

@interface RNSelectableTextView : RCTBaseTextInputView

@property (nonnull, nonatomic, copy) NSString *value;
@property (nonatomic, copy) RCTDirectEventBlock onSelection;
@property (nullable, nonatomic, copy) NSArray<NSString *> *menuItems;
@property (nullable, nonatomic, copy) NSArray<NSString *> *menuItemsExtend;
@property (nonatomic, copy) RCTDirectEventBlock onHighlightPress;

// Declare the showMenuItemsExtend method
- (void)showMenuItemsExtend;

@end

NS_ASSUME_NONNULL_END
