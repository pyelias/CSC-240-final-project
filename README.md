To use:
`java Main.java splitData` to generate training and testing data
`java Main.java buildModel` to build a model from training data
`java Main.java predict` to make predictions on emails from a file (unlabeled_test.csv by default)
`java Main.java evaluate` to evaluate model accuracy (on test.csv by default)

Method:
The model only looks at which words are present in spam vs ham emails.
Words that appear more often in one type of email compared to the other are selected as features.
In the model files (cat-all.csv, cat-ham.csv, cat-spam.csv), feature words are stored along with their probability of occuring in each type of email.

Accuracy:
Usuallty >90%, varies depending on training dataset